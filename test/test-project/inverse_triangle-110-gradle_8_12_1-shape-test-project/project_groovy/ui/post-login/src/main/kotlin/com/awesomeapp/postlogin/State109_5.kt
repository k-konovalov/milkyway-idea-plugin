package com.awesomeapp.postlogin

sealed class State109_5 {
    data object Loading : State109_5()
    data class Success(val data: String) : State109_5()
    data class Error(val message: String) : State109_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}