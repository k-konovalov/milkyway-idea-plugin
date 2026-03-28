package com.awesomeapp.checkoutidentity

sealed class State152_10 {
    data object Loading : State152_10()
    data class Success(val data: String) : State152_10()
    data class Error(val message: String) : State152_10()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}