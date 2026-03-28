package com.awesomeapp.articlecontact

sealed class State91_5 {
    data object Loading : State91_5()
    data class Success(val data: String) : State91_5()
    data class Error(val message: String) : State91_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}