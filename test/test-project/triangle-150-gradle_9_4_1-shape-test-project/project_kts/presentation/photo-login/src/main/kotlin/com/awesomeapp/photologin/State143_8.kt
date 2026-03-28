package com.awesomeapp.photologin

sealed class State143_8 {
    data object Loading : State143_8()
    data class Success(val data: String) : State143_8()
    data class Error(val message: String) : State143_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}