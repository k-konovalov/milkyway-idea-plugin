package com.awesomeapp.checkoutcheckout

sealed class State201_6 {
    data object Loading : State201_6()
    data class Success(val data: String) : State201_6()
    data class Error(val message: String) : State201_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}