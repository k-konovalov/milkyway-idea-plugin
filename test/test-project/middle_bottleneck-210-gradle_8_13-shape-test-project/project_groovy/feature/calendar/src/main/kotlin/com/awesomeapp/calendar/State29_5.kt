package com.awesomeapp.calendar

sealed class State29_5 {
    data object Loading : State29_5()
    data class Success(val data: String) : State29_5()
    data class Error(val message: String) : State29_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}