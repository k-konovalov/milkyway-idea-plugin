package com.awesomeapp.settingidentity

sealed class State167_5 {
    data object Loading : State167_5()
    data class Success(val data: String) : State167_5()
    data class Error(val message: String) : State167_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}