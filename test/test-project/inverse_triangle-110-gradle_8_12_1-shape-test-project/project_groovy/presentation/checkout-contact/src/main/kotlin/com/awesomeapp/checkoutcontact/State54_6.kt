package com.awesomeapp.checkoutcontact

sealed class State54_6 {
    data object Loading : State54_6()
    data class Success(val data: String) : State54_6()
    data class Error(val message: String) : State54_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}