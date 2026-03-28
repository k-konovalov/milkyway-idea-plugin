package com.awesomeapp.podcastlogin

sealed class State141_6 {
    data object Loading : State141_6()
    data class Success(val data: String) : State141_6()
    data class Error(val message: String) : State141_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}