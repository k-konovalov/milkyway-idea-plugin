package com.awesomeapp.messagecontact

sealed class State67_8 {
    data object Loading : State67_8()
    data class Success(val data: String) : State67_8()
    data class Error(val message: String) : State67_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}