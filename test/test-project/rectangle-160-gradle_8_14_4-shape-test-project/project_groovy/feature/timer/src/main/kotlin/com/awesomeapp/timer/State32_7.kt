package com.awesomeapp.timer

sealed class State32_7 {
    data object Loading : State32_7()
    data class Success(val data: String) : State32_7()
    data class Error(val message: String) : State32_7()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}