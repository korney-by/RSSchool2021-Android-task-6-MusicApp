package com.korneysoft.rsschool2021_android_task_6_musicapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "T6-MainViewModel"

class MainViewModel @Inject constructor() : ViewModel(), PlayerControl {

    private val _commandToPlayerFlow = MutableSharedFlow<PlayerCommand>()
    val commandToPlayerFlow = _commandToPlayerFlow.asSharedFlow()

    private val _setPlaybackPositionFlow = MutableSharedFlow<Long>()
    val setPlaybackPositionFlow = _setPlaybackPositionFlow.asSharedFlow()

    override fun seekTo(position: Long) {
        viewModelScope.launch {
            _setPlaybackPositionFlow.emit(position)
        }
    }

    private fun postCommand(command: PlayerCommand) {
        viewModelScope.launch {
            _commandToPlayerFlow.emit(command)
        }
    }

    override fun next() {
        Log.d(TAG, "next")
        postCommand(PlayerCommand.NEXT)
    }

    override fun previous() {
        Log.d(TAG, "previous")
        postCommand(PlayerCommand.PREVIOUS)
    }

    override fun play() {
        Log.d(TAG, "play")
        postCommand(PlayerCommand.PLAY)

    }

    override fun pause() {
        Log.d(TAG, "pause")
        postCommand(PlayerCommand.PAUSE)
    }

    override fun stop() {
        Log.d(TAG, "stop")
        postCommand(PlayerCommand.STOP)
    }
}
