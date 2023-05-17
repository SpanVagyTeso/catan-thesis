package com.catan.sdk.dto.login

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.LOGIN_SUCCESS

data class LoginSuccessDto(
    val sessionId: String
) : DtoType(LOGIN_SUCCESS)
