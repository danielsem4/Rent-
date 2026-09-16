package org.example.rent.auth.presentation.qrscanner

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

class QrScannerViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QrScannerState())
    val state = _state.asStateFlow()

    private val eventChannel = Channel<QrScannerEvent>()
    val events = eventChannel.receiveAsFlow()

    fun onAction(action: QrScannerAction) {
        when (action) {
            is QrScannerAction.OnQrScanned -> onScanned(action.payload)

            is QrScannerAction.OnPermissionResult ->
                _state.update { it.copy(permission = action.state) }

            QrScannerAction.OnRetry ->
                _state.update { it.copy(error = null, hasScanned = false) }
        }
    }

    private fun onScanned(payload: String) {
        val current = _state.value
        if (current.isLoading || current.hasScanned) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, hasScanned = true, error = null) }
            authRepository.loginWithQr(payload)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    eventChannel.send(QrScannerEvent.ScanSuccess)
                }
                .onFailure { error ->
                    // Reset the guard so the user can rescan after a failure.
                    _state.update { it.copy(isLoading = false, hasScanned = false, error = error.toUiText()) }
                }
        }
    }
}
