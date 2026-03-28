package com.awesomeapp.videoidentity

sealed class State191_8 {
    data object Loading : State191_8()
    data class Success(val data: String) : State191_8()
    data class Error(val message: String) : State191_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}