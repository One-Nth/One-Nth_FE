package com.example.onenthapp.data.userset

data class UserSettings(
    val scrapAlertSummary: AlertSummary,
    val reviewAlertSummary: AlertSummary,
    val keywordAlertSummaryList: List<KeywordAlertSummary>
)
