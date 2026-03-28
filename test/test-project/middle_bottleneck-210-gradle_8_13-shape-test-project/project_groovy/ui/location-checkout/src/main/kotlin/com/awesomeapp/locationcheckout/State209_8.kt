package com.awesomeapp.locationcheckout

sealed class State209_8 {
    data object Loading : State209_8()
    data class Success(val data: String) : State209_8()
    data class Error(val message: String) : State209_8()

    companion object {
        fun loading() = Loading
        fun success(data: String) = Success(data)
        fun error(message: String) = Error(message)
    }
}