package com.awesomeapp.audiologin

sealed class State146_5 {
    data object Loading : State146_5()
    data class Success(val data: String) : State146_5()
    data class Error(val message: String) : State146_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}