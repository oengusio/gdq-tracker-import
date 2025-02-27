package net.oengus.gdqimporter.objects

import kotlinx.serialization.Serializable

@Serializable
data class ODataList<T>(val data: List<T>)

@Serializable
data class OScheduleInfo(
    val id: Int,
    val marathonId: String,
    val name: String,
    val slug: String,
    val published: Boolean,
)

@Serializable
data class OSchedule(val name: String, val lines: List<OLine>)

@Serializable
data class OLine(
    val id: Int,
    val game: String,
    val console: String,
    val emulated: Boolean,
    val ratio: String,
    val type: String,
    val runners: List<ORunner>,
    val category: String,
    val estimate: String,
    val setupTime: String,
    val position: Int,
    val setupBlock: Boolean,
)

@Serializable
data class OProfile(
    val username: String,
    val displayName: String,
)

@Serializable
data class ORunner(
    val runnerName: String,
    val profile: OProfile?,
)
