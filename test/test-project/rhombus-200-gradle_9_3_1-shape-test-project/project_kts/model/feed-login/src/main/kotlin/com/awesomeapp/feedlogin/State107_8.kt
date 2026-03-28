package com.awesomeapp.feedlogin

sealed class State107_8 {
    data object Loading : State107_8()
    data class Success(val data: String) : State107_8()
    data class Error(val message: String) : State107_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}