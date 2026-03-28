package com.awesomeapp.post

sealed class State11_8 {
    data object Loading : State11_8()
    data class Success(val data: String) : State11_8()
    data class Error(val message: String) : State11_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}