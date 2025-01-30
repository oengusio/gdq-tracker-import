package net.oengus.gdqimporter

import net.oengus.gdqimporter.objects.TRun

// TODO: make user input tracker url (eg https://tracker.gamesdonequick.com/tracker/)
class TrackerApi(private val trackerUrl: String) {
    var session: Any? = null

    fun login(username: String, password: String): Boolean {
        // TODO: implement actual login to tracker, probably copy what layouts is doing
        return true
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
}
