package net.oengus.gdqimporter

import net.oengus.gdqimporter.http.CookieJar
import net.oengus.gdqimporter.objects.TEventSearch
import net.oengus.gdqimporter.objects.TResults
import net.oengus.gdqimporter.objects.TRun
import net.oengus.gdqimporter.objects.TRunner
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets


class TrackerApi(private val trackerUrl: String) {
    private val cookieJar = CookieJar()
    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .build()


    private var crossSiteToken = fetchCSRFToken()
//    private val crossSiteToken = "123"

    fun login(username: String, password: String): Boolean {
        val loginUrl = getAdminUrl("/login/")

        val formBody = FormBody.Builder(StandardCharsets.UTF_8)
            .add("username", username)
            .add("password", password)
            .add("csrfmiddlewaretoken", crossSiteToken ?: "")
            .add("next", "/admin/") // needed because we follow redirects
            .build()

        val req = Request.Builder()
            .url(loginUrl)
            .post(formBody)
            .addHeader("referer", loginUrl)
            .build()

        client.newCall(req).execute().use { response ->
            // we have a new csrf token once we've logged in
            if (response.code == 200) {
                crossSiteToken = fetchCSRFToken()
                return true
            }

            return false
        }
    }

    fun findEventIdByShort(short: String): Int? {
        val url = getTrackerUrl("/search/?short=$short&type=event")

        val req = Request.Builder()
            .url(url)
            .build()

        client.newCall(req).execute().use { response ->
            val eventList = response.body?.let {
                val jsonString = it.string()

                json.decodeFromString<List<TEventSearch>>(jsonString)
            }

            return eventList?.firstOrNull()?.pk
        }
    }

    fun findRunner(username: String) = findRunnerFromUrl(getTrackerUrl("/api/v2/talent?name=$username"))

    fun findRunnerById(id: Int) = findRunnerFromUrl(getTrackerUrl("/api/v2/talent?id=$id"))

    private fun findRunnerFromUrl(url: String): TRunner? {
        val req = Request.Builder()
            .url(url)
            .build()

        client.newCall(req).execute().use { response ->
            val userList = response.body?.let {
                val jsonString = it.string()

                json.decodeFromString<TResults<TRunner>>(jsonString)
            }

            return userList?.results?.firstOrNull()
        }
    }

    fun createRunner(username: String): Int? {
        val talentAddUrl = getAdminUrl("/tracker/talent/add/")

        val formBody = FormBody.Builder(StandardCharsets.UTF_8)
            .add("name", username)
            .add("stream", "")
            .add("twitter", "")
            .add("youtube", "")
            .add("platform", "TWITCH")
            .add("pronouns", "")
            .add("donor", "")
            .add("csrfmiddlewaretoken", crossSiteToken ?: "")
            .add("_continue", "Save+and+continue+editing") // ensure proper redirect
            .build()

        val req = Request.Builder()
            .url(talentAddUrl)
            .post(formBody)
            .addHeader("referer", talentAddUrl)
            .build()

        client.newCall(req).execute().use { response ->
            response.networkResponse?.let {
                val redirUrl = it.request.url.toString()

                if (redirUrl.endsWith("/change/")) {
                    // Is regex the right solution? I DON'T CARE
                    val regex = "([0-9]+)".toRegex()
                    val matchResult = regex.find(redirUrl) ?: return null

                    return matchResult.groupValues[1].toIntOrNull()
                }
            }
        }

        return null
    }

    fun fetchRuns(eventId: Int): List<TRun> {
        val url = getTrackerUrl("/api/v2/events/$eventId/runs")

        val req = Request.Builder()
            .url(url)
            .build()


        client.newCall(req).execute().use { response ->
            val runList = response.body?.let {
                val jsonString = it.string()

                json.decodeFromString<TResults<TRun>>(jsonString)
            }

            return runList?.results ?: emptyList()
        }
    }

    fun clearSchedule(eventId: Int) {
        val runIds = fetchRuns(eventId).map { it.id }

        if (runIds.isEmpty()) {
            return
        }

        val deleteUrl = getAdminUrl("/tracker/speedrun/")

        val formBody = FormBody.Builder(StandardCharsets.UTF_8)
            .apply {
                runIds.forEach {
                    add("_selected_action", it.toString())
                }
            }
            .add("action", "delete_selected")
            .add("post", "yes")
            .add("csrfmiddlewaretoken", crossSiteToken ?: "")
            .build()

        val req = Request.Builder()
            .url(deleteUrl)
            .post(formBody)
            .addHeader("referer", deleteUrl)
            .build()

        client.newCall(req).execute().use {
            if (it.code != 200) {
                throw RuntimeException("Failed to clear schedule, status code: ${it.code}")
            }
        }
    }

    fun insertRun(eventId: Int, run: TRun) {
        val insertUrl = getAdminUrl("/tracker/speedrun/add/")

        val formBody = FormBody.Builder(StandardCharsets.UTF_8)
            .add("name", run.name)
            .add("display_name", run.displayName)
            .add("twitch_name", run.twitchName)
            .add("category", run.category)
            .add("console", run.console)
            .add("release_year", "")
            .add("description", "")
            .add("event", eventId.toString())
            .add("initial-event", "0")
            .add("order", run.order.toString())
            .add("anchor_time_0", "")
            .add("anchor_time_1", "")
            .add("run_time", run.runTime)
            .add("setup_time", run.setupTime)
            .apply {
                run.runners.forEach {
                    add("runners", it.id.toString())
                }
            }
            .add("onsite", "ONSITE")
            .add("tech_notes", "")
            .add("layout", "")
            .add("priority_tag", "")
            .add("video_links-TOTAL_FORMS", "1")
            .add("video_links-INITIAL_FORMS", "0")
            .add("video_links-MIN_NUM_FORMS", "0")
            .add("video_links-MAX_NUM_FORMS", "1000")
            .add("video_links-0-link_type", "")
            .add("video_links-0-url", "")
            .add("video_links-0-id", "")
            .add("video_links-0-run", "")
            .add("video_links-__prefix__-link_type", "")
            .add("video_links-__prefix__-url", "")
            .add("video_links-__prefix__-id", "")
            .add("video_links-__prefix__-run", "")
            .add("_continue", "Save+and+continue+editing")
            .add("csrfmiddlewaretoken", crossSiteToken ?: "")
            .build()

        val req = Request.Builder()
            .url(insertUrl)
            .post(formBody)
            .addHeader("referer", insertUrl)
            .build()

        client.newCall(req).execute().use {
            if (it.code != 200) {
                throw RuntimeException("Failed to insert run ${run.name}, status code: ${it.code}")
            }
        }
    }

    private fun fetchCSRFToken(): String? {
        val loginUrl = getAdminUrl("/login/")

        val req = Request.Builder()
            .url(loginUrl)
            .build()

        client.newCall(req).execute().use { response ->
            val cookieHeader = response.headers["set-cookie"] ?: return null

            return cookieHeader.substring(
                cookieHeader.indexOf("csrftoken=") + "csrftoken=".length,
                cookieHeader.indexOf(";")
            )
        }
    }

    private val trackerBaseUrl: String
        get() {
            var firstBase = this.trackerUrl.replace("/tracker", "")

            if (firstBase.endsWith("/")) {
                firstBase = firstBase.substring(0, firstBase.length - 1)
            }

            return firstBase
        }

    private fun getTrackerUrl(path: String): String {
        return "$trackerBaseUrl/tracker$path"
    }

    private fun getAdminUrl(path: String): String {
        return "$trackerBaseUrl/admin$path"
    }
}
