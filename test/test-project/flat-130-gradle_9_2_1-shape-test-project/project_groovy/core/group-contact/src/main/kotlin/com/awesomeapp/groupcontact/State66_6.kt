package com.awesomeapp.groupcontact

sealed class State66_6 {
    data object Loading : State66_6()
    data class Success(val data: String) : State66_6()
    data class Error(val message: String) : State66_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}