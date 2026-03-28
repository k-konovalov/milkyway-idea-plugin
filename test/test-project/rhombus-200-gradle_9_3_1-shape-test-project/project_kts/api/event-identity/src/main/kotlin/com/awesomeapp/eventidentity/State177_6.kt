package com.awesomeapp.eventidentity

sealed class State177_6 {
    data object Loading : State177_6()
    data class Success(val data: String) : State177_6()
    data class Error(val message: String) : State177_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}