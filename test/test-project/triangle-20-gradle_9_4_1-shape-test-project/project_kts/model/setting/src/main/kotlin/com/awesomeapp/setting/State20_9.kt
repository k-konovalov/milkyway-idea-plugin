package com.awesomeapp.setting

sealed class State20_9 {
    data object Loading : State20_9()
    data class Success(val data: String) : State20_9()
    data class Error(val message: String) : State20_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}