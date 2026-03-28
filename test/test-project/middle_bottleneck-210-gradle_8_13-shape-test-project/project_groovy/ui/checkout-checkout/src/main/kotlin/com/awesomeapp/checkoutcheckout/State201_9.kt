package com.awesomeapp.checkoutcheckout

sealed class State201_9 {
    data object Loading : State201_9()
    data class Success(val data: String) : State201_9()
    data class Error(val message: String) : State201_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}