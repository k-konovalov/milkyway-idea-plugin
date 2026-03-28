package com.awesomeapp.maplogin

sealed class State136_10 {
    data object Loading : State136_10()
    data class Success(val data: String) : State136_10()
    data class Error(val message: String) : State136_10()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}