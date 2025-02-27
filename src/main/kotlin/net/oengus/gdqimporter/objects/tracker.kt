package net.oengus.gdqimporter.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TResults<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>,
)

@Serializable
data class TRunner(
    val type: String,
    val id: Int,
    val name: String,
)

@Serializable
data class TRun(
    val type: String,
    val id: Int,
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("twitch_name")
    val twitchName: String,
    val description: String,
    val category: String,
    val coop: Boolean,
    val onsite: String,
    val console: String,
    @SerialName("release_year")
    val releaseYear: Int?,
    val runners: List<TRunner>,
    // hosts
    // comentators
    // starttime
    // endtime
    val order: Int
    // run_time
    // setup_time
)
