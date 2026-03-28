package com.awesomeapp.commentcheckout

sealed class State206_5 {
    data object Loading : State206_5()
    data class Success(val data: String) : State206_5()
    data class Error(val message: String) : State206_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}