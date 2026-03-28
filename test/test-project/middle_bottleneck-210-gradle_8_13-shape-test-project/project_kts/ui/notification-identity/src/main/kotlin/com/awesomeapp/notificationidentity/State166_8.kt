package com.awesomeapp.notificationidentity

sealed class State166_8 {
    data object Loading : State166_8()
    data class Success(val data: String) : State166_8()
    data class Error(val message: String) : State166_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}