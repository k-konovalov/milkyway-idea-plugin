package com.awesomeapp.noteidentity

sealed class State182_5 {
    data object Loading : State182_5()
    data class Success(val data: String) : State182_5()
    data class Error(val message: String) : State182_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}