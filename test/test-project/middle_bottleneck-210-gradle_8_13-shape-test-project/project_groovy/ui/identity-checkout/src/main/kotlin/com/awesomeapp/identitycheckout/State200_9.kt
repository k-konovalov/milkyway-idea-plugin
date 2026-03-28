package com.awesomeapp.identitycheckout

sealed class State200_9 {
    data object Loading : State200_9()
    data class Success(val data: String) : State200_9()
    data class Error(val message: String) : State200_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}