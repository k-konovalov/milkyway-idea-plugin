package com.awesomeapp.profilecheckout

sealed class State204_8 {
    data object Loading : State204_8()
    data class Success(val data: String) : State204_8()
    data class Error(val message: String) : State204_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}