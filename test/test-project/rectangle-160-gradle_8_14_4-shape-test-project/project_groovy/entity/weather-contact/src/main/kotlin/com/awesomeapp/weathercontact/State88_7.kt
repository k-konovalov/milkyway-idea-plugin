package com.awesomeapp.weathercontact

sealed class State88_7 {
    data object Loading : State88_7()
    data class Success(val data: String) : State88_7()
    data class Error(val message: String) : State88_7()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}