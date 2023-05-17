package com.catan.sdk.dto.register

import com.catan.sdk.dto.DtoType
import com.catan.sdk.dto.REGISTER_SUCCES

data class RegisterSuccessDto(
    val success: Boolean
) : DtoType(REGISTER_SUCCES)
