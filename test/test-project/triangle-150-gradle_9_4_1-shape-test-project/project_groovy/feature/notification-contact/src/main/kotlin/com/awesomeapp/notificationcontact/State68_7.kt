package com.awesomeapp.notificationcontact

sealed class State68_7 {
    data object Loading : State68_7()
    data class Success(val data: String) : State68_7()
    data class Error(val message: String) : State68_7()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}