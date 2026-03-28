package com.awesomeapp.playlistcontact

sealed class State98_5 {
    data object Loading : State98_5()
    data class Success(val data: String) : State98_5()
    data class Error(val message: String) : State98_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}