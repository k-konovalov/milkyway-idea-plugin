package com.awesomeapp.photoidentity

sealed class State192_8 {
    data object Loading : State192_8()
    data class Success(val data: String) : State192_8()
    data class Error(val message: String) : State192_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}