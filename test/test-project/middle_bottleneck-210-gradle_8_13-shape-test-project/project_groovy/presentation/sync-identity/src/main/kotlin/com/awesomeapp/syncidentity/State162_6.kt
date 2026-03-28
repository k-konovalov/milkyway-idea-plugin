package com.awesomeapp.syncidentity

sealed class State162_6 {
    data object Loading : State162_6()
    data class Success(val data: String) : State162_6()
    data class Error(val message: String) : State162_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}