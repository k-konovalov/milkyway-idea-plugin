package com.awesomeapp.loginidentity

sealed class State150_8 {
    data object Loading : State150_8()
    data class Success(val data: String) : State150_8()
    data class Error(val message: String) : State150_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}