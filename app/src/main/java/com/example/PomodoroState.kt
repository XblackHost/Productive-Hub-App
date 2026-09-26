package com.example

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PomodoroState {
    private val mainHandler = Handler(Looper.getMainLooper())

    private const val DEFAULT_FOCUS_MINUTES = 25
    private const val DEFAULT_BREAK_MINUTES = 5

    data class State(
        val totalSeconds: Int = DEFAULT_FOCUS_MINUTES * 60,
        val remainingSeconds: Int = DEFAULT_FOCUS_MINUTES * 60,
        val isRunning: Boolean = false,
        val isBreak: Boolean = false,
        val completedPomodoros: Int = 0
    ) {
        val formattedTime: String
            get() {
                val m = remainingSeconds / 60
                val s = remainingSeconds % 60
                return "%02d:%02d".format(m, s)
            }

        val progress: Float
            get() = if (totalSeconds > 0) {
                (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
            } else 0f
    }

    private val _stateFlow = MutableStateFlow(State())
    val stateFlow: StateFlow<State> = _stateFlow.asStateFlow()

    private val ticker = object : Runnable {
        override fun run() {
            val curr = _stateFlow.value
            if (!curr.isRunning) return

            if (curr.remainingSeconds <= 1) {
                // Session finished
                if (!curr.isBreak) {
                    val nextBreakSeconds = DEFAULT_BREAK_MINUTES * 60
                    _stateFlow.value = curr.copy(
                        totalSeconds = nextBreakSeconds,
                        remainingSeconds = nextBreakSeconds,
                        isRunning = false,
                        isBreak = true,
                        completedPomodoros = curr.completedPomodoros + 1
                    )
                } else {
                    val nextFocusSeconds = DEFAULT_FOCUS_MINUTES * 60
                    _stateFlow.value = curr.copy(
                        totalSeconds = nextFocusSeconds,
                        remainingSeconds = nextFocusSeconds,
                        isRunning = false,
                        isBreak = false
                    )
                }
            } else {
                _stateFlow.value = curr.copy(remainingSeconds = curr.remainingSeconds - 1)
                mainHandler.postDelayed(this, 1000L)
            }
        }
    }

    fun start() {
        if (_stateFlow.value.isRunning) return
        _stateFlow.value = _stateFlow.value.copy(isRunning = true)
        mainHandler.postDelayed(ticker, 1000L)
    }

    fun pause() {
        mainHandler.removeCallbacks(ticker)
        _stateFlow.value = _stateFlow.value.copy(isRunning = false)
    }

    fun reset() {
        mainHandler.removeCallbacks(ticker)
        val defaultSec = DEFAULT_FOCUS_MINUTES * 60
        _stateFlow.value = _stateFlow.value.copy(
            totalSeconds = defaultSec,
            remainingSeconds = defaultSec,
            isRunning = false,
            isBreak = false
        )
    }

    fun setMode(isBreak: Boolean, minutes: Int) {
        mainHandler.removeCallbacks(ticker)
        val sec = minutes * 60
        _stateFlow.value = _stateFlow.value.copy(
            totalSeconds = sec,
            remainingSeconds = sec,
            isRunning = false,
            isBreak = isBreak
        )
    }
}
