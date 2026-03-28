package com.awesomeapp.profilecontact

sealed class State57_6 {
    data object Loading : State57_6()
    data class Success(val data: String) : State57_6()
    data class Error(val message: String) : State57_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}