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
data class OLine(val runner: String, val estimate: String)

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
