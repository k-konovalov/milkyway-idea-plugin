package com.awesomeapp.audioidentity

sealed class State195_8 {
    data object Loading : State195_8()
    data class Success(val data: String) : State195_8()
    data class Error(val message: String) : State195_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}