package com.awesomeapp.photo

sealed class State45_8 {
    data object Loading : State45_8()
    data class Success(val data: String) : State45_8()
    data class Error(val message: String) : State45_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}