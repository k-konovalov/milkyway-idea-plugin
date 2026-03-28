package com.awesomeapp.forecastlogin

sealed class State138_6 {
    data object Loading : State138_6()
    data class Success(val data: String) : State138_6()
    data class Error(val message: String) : State138_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}