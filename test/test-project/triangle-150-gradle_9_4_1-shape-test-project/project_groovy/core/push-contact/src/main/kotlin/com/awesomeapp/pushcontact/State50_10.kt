package com.awesomeapp.pushcontact

sealed class State50_10 {
    data object Loading : State50_10()
    data class Success(val data: String) : State50_10()
    data class Error(val message: String) : State50_10()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}