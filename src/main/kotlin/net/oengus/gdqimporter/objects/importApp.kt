package net.oengus.gdqimporter.objects

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val oengusUrl: String,
    val trackerUrl: String,
    val trackerUsername: String,
    val trackerPassword: String,
)

data class ScheduleFetchSettings(
    val trackerEventId: Int,
    val marathonId: String,
    val scheduleSlug: String,
    val padHour: Boolean,
)
