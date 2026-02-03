package com.nima.app.imanage.data.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ToolbarEventBus {

    private val _events = MutableSharedFlow<ToolbarEvent>()
    val events = _events.asSharedFlow()

    suspend fun send(event: ToolbarEvent) {
        _events.emit(event)
    }
}

sealed interface ToolbarEvent {
    data object ToggleSensitive : ToolbarEvent
    data object ToggleEdit : ToolbarEvent
}