package com.awesomeapp.statusidentity

sealed class State173_5 {
    data object Loading : State173_5()
    data class Success(val data: String) : State173_5()
    data class Error(val message: String) : State173_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}