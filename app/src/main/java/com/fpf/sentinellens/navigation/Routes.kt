package com.fpf.sentinellens.navigation

object Routes {
    const val SURVEILLANCE = "surveillance"
    const val WATCHLIST = "watchlist"
    const val HELP = "help"
    const val LOG = "log"
    const val TEST = "test"

    const val SETTINGS = "settings"
    const val ADD_PERSON = "addPerson"
    const val SETTINGS_DETAIL = "settings_detail/{type}"
    const val DONATE = "donate"
    fun settingsDetail(type: String) = "settings_detail/$type"
}

object SettingsRoutes {
    const val THRESHOLD = "threshold"
    const val TELEGRAM = "telegram"
}
