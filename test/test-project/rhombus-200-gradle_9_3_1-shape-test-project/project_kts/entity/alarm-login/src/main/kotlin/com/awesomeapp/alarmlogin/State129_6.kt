package com.awesomeapp.alarmlogin

sealed class State129_6 {
    data object Loading : State129_6()
    data class Success(val data: String) : State129_6()
    data class Error(val message: String) : State129_6()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}