package com.awesomeapp.mapidentity

sealed class State185_10 {
    data object Loading : State185_10()
    data class Success(val data: String) : State185_10()
    data class Error(val message: String) : State185_10()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}