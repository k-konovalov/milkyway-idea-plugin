package com.awesomeapp.postcheckout

sealed class State207_9 {
    data object Loading : State207_9()
    data class Success(val data: String) : State207_9()
    data class Error(val message: String) : State207_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}