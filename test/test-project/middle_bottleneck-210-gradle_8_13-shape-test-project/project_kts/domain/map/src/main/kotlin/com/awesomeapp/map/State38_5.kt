package com.awesomeapp.map

sealed class State38_5 {
    data object Loading : State38_5()
    data class Success(val data: String) : State38_5()
    data class Error(val message: String) : State38_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}