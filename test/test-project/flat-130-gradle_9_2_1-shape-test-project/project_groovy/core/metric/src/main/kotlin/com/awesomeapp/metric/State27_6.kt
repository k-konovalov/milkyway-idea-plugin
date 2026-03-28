package com.awesomeapp.metric

sealed class State27_6 {
    data object Loading : State27_6()
    data class Success(val data: String) : State27_6()
    data class Error(val message: String) : State27_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}