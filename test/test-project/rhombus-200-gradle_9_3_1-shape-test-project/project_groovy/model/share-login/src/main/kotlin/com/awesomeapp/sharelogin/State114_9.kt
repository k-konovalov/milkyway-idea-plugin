package com.awesomeapp.sharelogin

sealed class State114_9 {
    data object Loading : State114_9()
    data class Success(val data: String) : State114_9()
    data class Error(val message: String) : State114_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}