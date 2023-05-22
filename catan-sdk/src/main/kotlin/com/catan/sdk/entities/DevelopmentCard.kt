package com.catan.sdk.entities

class DevelopmentCard(
    val developmentTypes: DevelopmentTypes,
    var hasToWait: Boolean = false
) {
    var used = false
}
