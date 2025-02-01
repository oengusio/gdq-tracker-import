package net.oengus.gdqimporter

import net.oengus.gdqimporter.http.CookieJar
import net.oengus.gdqimporter.objects.TRun
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

    fun login(username: String, password: String): Boolean {
        val crossSiteToken = fetchCSRFToken()
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
            return response.code == 200
        }
    }

    fun findRunner(username: String): Any? {
        return null
    }

    fun createRunner(username: String): Any {
        return ""
    }

    fun clearSchedule(eventShort: String) {
        //
    }

    fun insertRun(eventShort: String, run: TRun) {
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
