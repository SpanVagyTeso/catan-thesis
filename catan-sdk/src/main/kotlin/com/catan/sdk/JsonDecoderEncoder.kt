package com.catan.sdk

import com.catan.sdk.dto.DtoType
import com.catan.sdk.exception.BadDtoRuntimeException
import com.google.gson.Gson

fun DtoType.toJson(): String {
    val gson = Gson()
    return gson.toJson(this)
}

inline fun <reified T> String.toDto(): T {
    try {
        val gson = Gson()
        return gson.fromJson(this, T::class.java)
    } catch (e: Exception) {
        throw BadDtoRuntimeException("Bad dto")
    }
}
