package com.awesomeapp.shareidentity

sealed class State163_5 {
    data object Loading : State163_5()
    data class Success(val data: String) : State163_5()
    data class Error(val message: String) : State163_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}