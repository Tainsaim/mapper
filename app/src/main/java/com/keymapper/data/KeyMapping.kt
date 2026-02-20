package com.keymapper.data

data class KeyMapping(
    val id: String,
    val keyCode: Int,
    val keyLabel: String,
    val tapX: Float,
    val tapY: Float,
    val isEnabled: Boolean = true
)
