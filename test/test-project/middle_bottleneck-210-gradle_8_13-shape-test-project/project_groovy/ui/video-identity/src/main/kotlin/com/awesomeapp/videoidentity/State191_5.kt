package com.awesomeapp.videoidentity

sealed class State191_5 {
    data object Loading : State191_5()
    data class Success(val data: String) : State191_5()
    data class Error(val message: String) : State191_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}