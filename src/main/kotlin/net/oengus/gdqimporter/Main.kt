package net.oengus.gdqimporter

import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

fun String.isValidUrl(): Boolean {
    val urlRegex = """^https?://[\w\-]+(\.[\w\-]+)+[/#?]?.*$""".toRegex()
    return urlRegex.matches(this)
}

fun String.orDefault(default: String) = ifEmpty { default }

// TODO: if these settings are set we can ignore them
private fun askSetupQuestions(terminal: Terminal) {
    val reader = LineReaderBuilder.builder().terminal(terminal).build()

    val oengusUrl = reader.readLine("Enter Oengus URL (default: https://oengus.io/): ").orDefault("https://oengus.io/")

    terminal.writer().println("Oengus URL: $oengusUrl.")

    var trackerUrl = ""

    while (trackerUrl.isEmpty()) {
        trackerUrl = reader.readLine("Enter Tracker URL (example https://tracker.gamesdonequick.com/tracker/): ")

        if (!trackerUrl.isValidUrl()) {
            terminal.writer().println("That is not a valid url")
            trackerUrl = ""
        }
    }

    terminal.writer().println("Tracker URL: $trackerUrl.")

    terminal.writer().println("Settings stored in config.json. You can edit this later")
}

private fun askEventQuestions(terminal: Terminal) {
    val reader = LineReaderBuilder.builder().terminal(terminal).build()

    reader.printAbove("====== Oengus Event Config ======")

    val shortCode = reader.readLine("Shortcode (eg uksggre25): ")
    val scheduleSlug = reader.readLine("Slug of your schedule (eg stream-1): ")


    reader.printAbove("====== Tracker Event Config ======")

    val eventId = reader.readLine("Event short: ")
}

// Needs to create config.json
// - oengusUrl, default https://oengus.io/
// - trackerUrl, no default entered by user
fun main() {
    TerminalBuilder.builder()
        .system(true)
        .build()
        .use { terminal ->
            askSetupQuestions(terminal)
            askEventQuestions(terminal)
        }
}
