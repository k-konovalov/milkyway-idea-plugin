package com.awesomeapp.useridentity

sealed class State154_5 {
    data object Loading : State154_5()
    data class Success(val data: String) : State154_5()
    data class Error(val message: String) : State154_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}