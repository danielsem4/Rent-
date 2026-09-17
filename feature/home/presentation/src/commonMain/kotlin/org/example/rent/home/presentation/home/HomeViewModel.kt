package org.example.rent.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.rent.core.domain.util.onFailure
import org.example.rent.core.domain.util.onSuccess
import org.example.rent.core.presentation.util.toUiText
import org.example.rent.home.domain.WorkerRepository

class HomeViewModel(
    private val workerRepository: WorkerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnRetry -> loadProfile()
        }
    }

    private fun loadProfile() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            workerRepository.getMe()
                .onSuccess { worker ->
                    _state.update { it.copy(isLoading = false, worker = worker.toWorkerUi()) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toUiText()) }
                }
        }
    }
}
