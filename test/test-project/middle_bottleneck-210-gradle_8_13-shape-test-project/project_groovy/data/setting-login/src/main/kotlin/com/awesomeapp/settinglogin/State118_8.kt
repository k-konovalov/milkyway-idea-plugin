package com.awesomeapp.settinglogin

sealed class State118_8 {
    data object Loading : State118_8()
    data class Success(val data: String) : State118_8()
    data class Error(val message: String) : State118_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}