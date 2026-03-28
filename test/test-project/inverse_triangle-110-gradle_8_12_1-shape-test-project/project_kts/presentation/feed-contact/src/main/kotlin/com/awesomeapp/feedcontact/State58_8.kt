package com.awesomeapp.feedcontact

sealed class State58_8 {
    data object Loading : State58_8()
    data class Success(val data: String) : State58_8()
    data class Error(val message: String) : State58_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}