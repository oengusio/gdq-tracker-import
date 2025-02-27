package net.oengus.gdqimporter

import kotlinx.serialization.json.Json
import net.oengus.gdqimporter.http.CookieJar
import net.oengus.gdqimporter.objects.TResults
import net.oengus.gdqimporter.objects.TRun
import net.oengus.gdqimporter.objects.TRunner
import okhttp3.*
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

    fun findRunner(username: String) = findRunnerFromUrl(getTrackerUrl("/api/v2/talent?name=$username"))

    fun findRunnerById(id: Int) = findRunnerFromUrl(getTrackerUrl("/api/v2/talent?id=$id"))

    private fun findRunnerFromUrl(url: String): TRunner? {
        val req = Request.Builder()
            .url(url)
            .build()

        client.newCall(req).execute().use { response ->
            val userList = response.body?.let {
                val jsonString = it.string()

                Json.decodeFromString<TResults<TRunner>>(jsonString)
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

    fun clearSchedule(eventId: Int) {
        //
    }

    fun insertRun(eventId: Int, run: TRun) {
        //
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
