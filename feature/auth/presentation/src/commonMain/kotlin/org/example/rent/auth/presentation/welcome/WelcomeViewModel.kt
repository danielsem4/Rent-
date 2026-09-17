package org.example.rent.auth.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.rent.auth.domain.AuthRepository
import org.example.rent.core.domain.util.onFailure
import org.example.rent.core.domain.util.onSuccess
import org.example.rent.core.presentation.util.toUiText

class WelcomeViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<WelcomeEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: WelcomeAction) {
        when (action) {
            WelcomeAction.OnContinueWithPhone ->
                _state.update { it.copy(step = WelcomeStep.PHONE, error = null) }

            is WelcomeAction.OnPhoneNumberChange ->
                _state.update { it.copy(phoneNumber = action.phoneNumber, error = null) }

            is WelcomeAction.OnCodeChange ->
                _state.update { it.copy(code = action.code, error = null) }

            WelcomeAction.OnSubmitPhoneNumber -> submitPhoneNumber()

            WelcomeAction.OnSubmitCode -> submitCode()

            WelcomeAction.OnResendCode -> submitResend()

            is WelcomeAction.OnQrTokenReceived ->
                _state.update {
                    it.copy(step = WelcomeStep.CODE, qrToken = action.qrToken, code = "", error = null)
                }

            WelcomeAction.OnBack -> onBack()
        }
    }

    private fun onBack() {
        _state.update {
            when (it.step) {
                // The QR flow has no phone step, so its CODE step goes straight back to OPTIONS.
                WelcomeStep.CODE -> if (it.qrToken != null) {
                    it.copy(step = WelcomeStep.OPTIONS, qrToken = null, code = "", error = null)
                } else {
                    it.copy(step = WelcomeStep.PHONE, code = "", error = null)
                }
                WelcomeStep.PHONE -> it.copy(
                    step = WelcomeStep.OPTIONS,
                    phoneNumber = "",
                    code = "",
                    error = null,
                )
                WelcomeStep.OPTIONS -> it
            }
        }
    }

    private fun submitPhoneNumber() {
        val current = _state.value
        if (current.isLoading || current.phoneNumber.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.requestCode(current.phoneNumber)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, step = WelcomeStep.CODE, error = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    private fun submitCode() {
        val current = _state.value
        if (current.isLoading || current.code.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = current.qrToken?.let { token ->
                authRepository.verifyQrCode(token, current.code)
            } ?: authRepository.verifyCode(current.phoneNumber, current.code)
            result
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(WelcomeEvent.LoginSuccess)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }

    private fun submitResend() {
        val current = _state.value
        if (current.isLoading || current.isResending) return

        viewModelScope.launch {
            _state.update { it.copy(isResending = true, error = null) }
            val result = current.qrToken?.let { token ->
                authRepository.resendCode(qrToken = token)
            } ?: authRepository.resendCode(phoneNumber = current.phoneNumber)
            result
                .onSuccess {
                    _state.update { it.copy(isResending = false) }
                    eventChannel.send(WelcomeEvent.CodeResent)
                }
                .onFailure { error ->
                    _state.update { it.copy(isResending = false, error = error.toUiText()) }
                }
        }
    }
}
