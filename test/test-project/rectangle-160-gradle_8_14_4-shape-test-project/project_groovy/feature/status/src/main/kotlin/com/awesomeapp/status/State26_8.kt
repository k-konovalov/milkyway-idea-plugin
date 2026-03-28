package com.awesomeapp.status

sealed class State26_8 {
    data object Loading : State26_8()
    data class Success(val data: String) : State26_8()
    data class Error(val message: String) : State26_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}