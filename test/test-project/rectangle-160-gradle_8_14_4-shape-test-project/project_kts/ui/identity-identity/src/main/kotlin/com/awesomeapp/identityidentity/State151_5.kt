package com.awesomeapp.identityidentity

sealed class State151_5 {
    data object Loading : State151_5()
    data class Success(val data: String) : State151_5()
    data class Error(val message: String) : State151_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}