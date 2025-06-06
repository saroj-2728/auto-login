package com.data.login

data class SavedWifiCredential(
    val username: String,
    val password: String,
    val isPrimary: Boolean = false
)