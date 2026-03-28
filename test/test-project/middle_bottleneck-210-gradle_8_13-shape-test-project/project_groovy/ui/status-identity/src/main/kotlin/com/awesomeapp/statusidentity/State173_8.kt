package com.awesomeapp.statusidentity

sealed class State173_8 {
    data object Loading : State173_8()
    data class Success(val data: String) : State173_8()
    data class Error(val message: String) : State173_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}