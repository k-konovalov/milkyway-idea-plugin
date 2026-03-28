package com.awesomeapp.notelogin

sealed class State133_5 {
    data object Loading : State133_5()
    data class Success(val data: String) : State133_5()
    data class Error(val message: String) : State133_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}