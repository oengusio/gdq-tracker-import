package net.oengus.gdqimporter.http

import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.CookieJar as OkCookieJar

class CookieJar : OkCookieJar {
    private val cookieMap = mutableMapOf<String, MutableList<Cookie>>()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieMap[url.host] ?: emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookieList = cookieMap.getOrPut(url.host) { mutableListOf() }

        cookies.forEach { cookie ->
            val storedIndex = cookieList.indexOfFirst { it.name == cookie.name }

            if (storedIndex > -1) {
                cookieList[storedIndex] = cookie
            } else {
                cookieList.add(cookie)
            }
        }
    }

    override fun toString() = cookieMap.toString()
}
