package com.awesomeapp.alarmcontact

sealed class State80_9 {
    data object Loading : State80_9()
    data class Success(val data: String) : State80_9()
    data class Error(val message: String) : State80_9()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}