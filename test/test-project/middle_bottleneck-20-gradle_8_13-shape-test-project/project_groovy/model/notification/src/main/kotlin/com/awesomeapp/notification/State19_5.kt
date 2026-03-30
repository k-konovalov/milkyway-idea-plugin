package com.awesomeapp.notification

sealed class State19_5 {
    data object Loading : State19_5()
    data class Success(val data: String) : State19_5()
    data class Error(val message: String) : State19_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}