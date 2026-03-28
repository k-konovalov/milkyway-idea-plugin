package com.awesomeapp.sessioncontact

sealed class State71_8 {
    data object Loading : State71_8()
    data class Success(val data: String) : State71_8()
    data class Error(val message: String) : State71_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}