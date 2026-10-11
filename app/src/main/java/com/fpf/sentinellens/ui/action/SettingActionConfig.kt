package com.fpf.smartscan.ui.action

sealed interface SettingActionConfig {
    val enabled: Boolean
    val description: String?

    data class Button(
        val label: String,
        val onClick: () -> Unit,
        override val enabled: Boolean = true,
        override val description: String? = null,
        ) : SettingActionConfig

    data class Switch(
        val label: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        override val enabled: Boolean = true,
        override val description: String? = null,
        ) : SettingActionConfig
}