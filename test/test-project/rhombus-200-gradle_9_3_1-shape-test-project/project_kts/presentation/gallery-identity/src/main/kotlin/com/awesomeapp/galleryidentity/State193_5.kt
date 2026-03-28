package com.awesomeapp.galleryidentity

sealed class State193_5 {
    data object Loading : State193_5()
    data class Success(val data: String) : State193_5()
    data class Error(val message: String) : State193_5()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}