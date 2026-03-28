package com.awesomeapp.playlist

sealed class State49_5 {
    data object Loading : State49_5()
    data class Success(val data: String) : State49_5()
    data class Error(val message: String) : State49_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}