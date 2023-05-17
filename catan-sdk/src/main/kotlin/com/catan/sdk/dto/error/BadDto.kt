package com.catan.sdk.dto.error

import com.catan.sdk.dto.BAD
import com.catan.sdk.dto.DtoType

data class BadDto(
    val error: String
) : DtoType(BAD)
