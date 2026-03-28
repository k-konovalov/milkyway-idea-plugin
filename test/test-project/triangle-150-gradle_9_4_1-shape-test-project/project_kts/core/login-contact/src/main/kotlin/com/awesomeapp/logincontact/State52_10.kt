package com.awesomeapp.logincontact

sealed class State52_10 {
    data object Loading : State52_10()
    data class Success(val data: String) : State52_10()
    data class Error(val message: String) : State52_10()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}