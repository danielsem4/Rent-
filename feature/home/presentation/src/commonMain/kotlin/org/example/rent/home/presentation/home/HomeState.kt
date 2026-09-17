package org.example.rent.home.presentation.home

import org.example.rent.core.presentation.util.UiText

data class HomeState(
    val worker: WorkerUi? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
)
