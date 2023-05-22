package com.catan.sdk.dto

const val LOBBY_BASE = "Lobby"
const val LOGIN_SUCCESS = "SUCCESSFULLOGIN"
const val LOGIN = "LOGIN"
const val REGISTER = "REGISTER"
const val REGISTER_SUCCES = "REGISTERSUCCESS"
const val BAD = "BAD"
const val STATS = "STATS"

open class DtoType(
    val type: String
)
