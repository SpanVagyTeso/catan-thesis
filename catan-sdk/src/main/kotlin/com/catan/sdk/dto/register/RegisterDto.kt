package com.catan.sdk.dto.register

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.REGISTER

data class RegisterDto(
    val username: String,
    val password: String
) : DtoType(REGISTER)
