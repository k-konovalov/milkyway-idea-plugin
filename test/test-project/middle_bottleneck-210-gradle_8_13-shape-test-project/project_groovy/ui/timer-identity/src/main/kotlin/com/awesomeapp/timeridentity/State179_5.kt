package com.awesomeapp.timeridentity

sealed class State179_5 {
    data object Loading : State179_5()
    data class Success(val data: String) : State179_5()
    data class Error(val message: String) : State179_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}