package com.arfinhosain.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequests(
    val username: String,
    val password: String
)