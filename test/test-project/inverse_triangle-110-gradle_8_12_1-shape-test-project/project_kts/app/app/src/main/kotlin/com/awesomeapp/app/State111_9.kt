package com.awesomeapp.app

sealed class State111_9 {
    data object Loading : State111_9()
    data class Success(val data: String) : State111_9()
    data class Error(val message: String) : State111_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}