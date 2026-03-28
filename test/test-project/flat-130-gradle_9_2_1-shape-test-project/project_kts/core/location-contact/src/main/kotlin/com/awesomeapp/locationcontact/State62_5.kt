package com.awesomeapp.locationcontact

sealed class State62_5 {
    data object Loading : State62_5()
    data class Success(val data: String) : State62_5()
    data class Error(val message: String) : State62_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}