package com.catan.sdk.dto.login

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.LOGIN

data class LoginDto(
    val username: String,
    val password: String
) : DtoType(LOGIN)
