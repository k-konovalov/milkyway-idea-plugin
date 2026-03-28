package com.awesomeapp.locationidentity

sealed class State160_9 {
    data object Loading : State160_9()
    data class Success(val data: String) : State160_9()
    data class Error(val message: String) : State160_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}