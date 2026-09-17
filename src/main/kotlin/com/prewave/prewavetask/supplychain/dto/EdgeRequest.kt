package com.prewave.prewavetask.supplychain.dto

import jakarta.validation.constraints.NotBlank

data class EdgeRequest(
    @field:NotBlank
    val source: String,
    @field:NotBlank
    val target: String
)