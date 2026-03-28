package com.awesomeapp.todologin

sealed class State134_8 {
    data object Loading : State134_8()
    data class Success(val data: String) : State134_8()
    data class Error(val message: String) : State134_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}