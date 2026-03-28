package com.awesomeapp.listlogin

sealed class State135_8 {
    data object Loading : State135_8()
    data class Success(val data: String) : State135_8()
    data class Error(val message: String) : State135_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}