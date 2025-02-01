package net.oengus.gdqimporter

import kotlinx.serialization.json.Json
import net.oengus.gdqimporter.objects.Settings
import org.jline.consoleui.elements.ConfirmChoice
import org.jline.consoleui.prompt.ConsolePrompt
import org.jline.consoleui.prompt.PromptResultItemIF
import org.jline.consoleui.prompt.builder.PromptBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

fun ConsolePrompt.runPrompt(builder: PromptBuilder.() -> Unit): Map<String, PromptResultItemIF> {
    val resultMap = mutableMapOf<String, PromptResultItemIF>()

    val pb = this.promptBuilder

    builder(pb)

    this.prompt(
        mutableListOf(),
        pb.build(),
        resultMap
    )

    return resultMap
}

private fun loadSettings(): Settings? {
    val settingsFile = File(SETTINGS_FILE_NAME)

    if (!settingsFile.exists()) {
        return null
    }

    return Json.decodeFromString(
        settingsFile.readText(StandardCharsets.UTF_8)
    )
}

private fun saveSettings(settings: Settings) {
    val jsonString = Json.encodeToString(settings)


    val settingsFile = File(SETTINGS_FILE_NAME)

    if (settingsFile.exists()) {
        settingsFile.delete()
    }

    Files.write(
        settingsFile.toPath(),
        jsonString.toByteArray(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE_NEW
    )
}

lateinit var tracker: TrackerApi

private fun setupTracker(settings: Settings): Boolean {
    tracker = TrackerApi(settings.trackerUrl)

    return tracker.login(settings.trackerUsername, settings.trackerPassword)
}

private fun askSetupQuestions(terminal: Terminal): Settings {
    val prompt = ConsolePrompt(terminal)

    val resultMap = prompt.runPrompt {
        createInputPrompt()
            .name("oengusUrl")
            .message("Oengus URL: ")
            .defaultValue("https://oengus.io/")
            .addPrompt()

        createInputPrompt()
            .name("trackerUrl")
            .message("Tracker url: ")
            .defaultValue("https://tracker.gamesdonequick.com/tracker/")
            .addPrompt()

        createInputPrompt()
            .name("trackerUsername")
            .message("Username for tracker: ")
            .addPrompt()

        createInputPrompt()
            .name("trackerPassword")
            .message("Password for tracker: ")
            .mask('*')
            .addPrompt()
    }

    println("\nresult=$resultMap")

    val settings = Settings(
        oengusUrl = resultMap["oengusUrl"]!!.result,
        trackerUrl = resultMap["trackerUrl"]!!.result,
        trackerUsername = resultMap["trackerUsername"]!!.result,
        trackerPassword = resultMap["trackerPassword"]!!.result,
    )

    println("\nLogging in to tracker.....")

    val success = setupTracker(settings)

    if (success) {
        println("Login successful!")

        saveSettings(settings)

        println("Settings stored in $SETTINGS_FILE_NAME. You can edit this later")
        println("Configuration complete, preparing import")

        // Sleep so people can read
        Thread.sleep(1500)

        return settings
    }

    println("Login failed, please check your credentials and try again")

    // Sleep so people can read
    Thread.sleep(1500)

    throw RuntimeException("Failed to setup tracker")
}

private fun askEventQuestions(terminal: Terminal) {
    val prompt = ConsolePrompt(terminal)

    val marathonShortResult = prompt.runPrompt {
        createInputPrompt()
            .name("eventShort")
            .message("Shortcode (eg uksggre25): ")
            .addPrompt()
    }

    println("Fetching schedules......")

    val schedulesResult = prompt.runPrompt {
        createListPrompt()
            .name("schedule")
            .message("Select your schedule")
            .newItem("test").name("Test item 1").add()
            .newItem("test2").name("Test item 2").add()
            .addPrompt()

        createConfirmPromp()
            .name("paddHour")
            .message("Add an hour padding to the last run as a buffer")
            .defaultValue(ConfirmChoice.ConfirmationValue.YES)
            .addPrompt()
    }
}

// Needs to create config.json
// - oengusUrl, default https://oengus.io/
// - trackerUrl, no default entered by user
fun main() {
    TerminalBuilder.builder()
        .system(true)
        .build()
        .use { terminal ->
            var storedSettings = loadSettings()

            println("Settings=$storedSettings")

            if (storedSettings == null) {
                storedSettings = askSetupQuestions(terminal)
            }


//            askEventQuestions(terminal)
        }
}
