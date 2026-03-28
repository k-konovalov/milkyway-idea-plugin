package com.awesomeapp.mediacontact

sealed class State96_8 {
    data object Loading : State96_8()
    data class Success(val data: String) : State96_8()
    data class Error(val message: String) : State96_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}