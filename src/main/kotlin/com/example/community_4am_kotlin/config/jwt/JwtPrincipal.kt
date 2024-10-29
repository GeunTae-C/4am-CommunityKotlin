package com.example.Community_4am_Kotlin.config.jwt



import java.io.Serializable
import java.security.Principal

class JwtPrincipal:Principal, Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
    private val username:String = ""

    override fun getName(): String {
        return username
    }
    override fun toString():String{
        return "JwtPrincipal{"+"+username='"+username+'\''+'}'
    }
}