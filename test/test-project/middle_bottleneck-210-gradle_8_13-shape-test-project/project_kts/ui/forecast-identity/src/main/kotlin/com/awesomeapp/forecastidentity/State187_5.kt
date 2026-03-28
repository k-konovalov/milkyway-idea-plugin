package com.awesomeapp.forecastidentity

sealed class State187_5 {
    data object Loading : State187_5()
    data class Success(val data: String) : State187_5()
    data class Error(val message: String) : State187_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}