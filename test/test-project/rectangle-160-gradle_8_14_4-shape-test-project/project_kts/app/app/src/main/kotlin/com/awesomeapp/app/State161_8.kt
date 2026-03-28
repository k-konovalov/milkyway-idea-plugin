package com.awesomeapp.app

sealed class State161_8 {
    data object Loading : State161_8()
    data class Success(val data: String) : State161_8()
    data class Error(val message: String) : State161_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}