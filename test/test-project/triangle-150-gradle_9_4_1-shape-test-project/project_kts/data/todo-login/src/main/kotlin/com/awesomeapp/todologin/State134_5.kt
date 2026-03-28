package com.awesomeapp.todologin

sealed class State134_5 {
    data object Loading : State134_5()
    data class Success(val data: String) : State134_5()
    data class Error(val message: String) : State134_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}