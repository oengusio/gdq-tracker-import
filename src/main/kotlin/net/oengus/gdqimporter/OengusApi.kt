package net.oengus.gdqimporter

import kotlinx.serialization.json.Json
import net.oengus.gdqimporter.objects.ODataList
import net.oengus.gdqimporter.objects.OSchedule
import net.oengus.gdqimporter.objects.OScheduleInfo
import okhttp3.OkHttpClient
import okhttp3.Request

class OengusApi(private val oengusUrl: String) {
    private val client = OkHttpClient.Builder()
        .build()

    private val baseUrl: String
        get() {
            var firstBase = this.oengusUrl

            if (firstBase.endsWith("/")) {
                firstBase = firstBase.substring(0, firstBase.length - 1)
            }

            return firstBase
        }

    fun fetchSchedules(marathonId: String): List<OScheduleInfo> {
        val req = Request.Builder()
            .url("$baseUrl/api/v2/marathons/$marathonId/schedules")
            .build()

        client.newCall(req).execute().use { response ->
            val scheduleInfos = response.body?.let {
                val jsonString = it.string()

                Json.decodeFromString<ODataList<OScheduleInfo>>(jsonString)
            }

            return scheduleInfos?.data ?: emptyList()
        }
    }

    fun fetchSchedule(marathonId: String, scheduleSlug: String): OSchedule {
        val req = Request.Builder()
            .url("$baseUrl/api/v2/marathons/$marathonId/schedules/for-slug/$scheduleSlug")
            .build()

        client.newCall(req).execute().use { response ->
            return response.body!!.let {
                val jsonString = it.string()

                Json.decodeFromString(jsonString)
            }
        }
    }
}
