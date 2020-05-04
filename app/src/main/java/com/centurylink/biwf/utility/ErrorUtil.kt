package com.centurylink.biwf.utility

class Errors : HashMap<String, Boolean>(){

    fun hasErrors(): Boolean{
        return size > 0
    }
}

data class ValidationException(val errors : Errors) : RuntimeException()