package com.awesomeapp.logincheckout

sealed class State199_5 {
    data object Loading : State199_5()
    data class Success(val data: String) : State199_5()
    data class Error(val message: String) : State199_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}