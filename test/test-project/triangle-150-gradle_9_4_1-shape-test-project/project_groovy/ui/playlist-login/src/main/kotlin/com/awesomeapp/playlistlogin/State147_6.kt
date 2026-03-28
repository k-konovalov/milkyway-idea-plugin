package com.awesomeapp.playlistlogin

sealed class State147_6 {
    data object Loading : State147_6()
    data class Success(val data: String) : State147_6()
    data class Error(val message: String) : State147_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}