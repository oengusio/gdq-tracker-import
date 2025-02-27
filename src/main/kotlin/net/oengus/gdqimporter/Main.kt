package net.oengus.gdqimporter

import kotlinx.serialization.json.Json
import net.oengus.gdqimporter.objects.ScheduleFetchSettings
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

lateinit var oengus: OengusApi

private fun setupOengus(settings: Settings) {
    oengus = OengusApi(settings.oengusUrl)
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

private fun askEventQuestions(terminal: Terminal): ScheduleFetchSettings? {
    val prompt = ConsolePrompt(terminal)

    val marathonShortResult = prompt.runPrompt {
        createInputPrompt()
            .name("eventShort")
            .message("Shortcode from tracker (eg uksggre25): ")
            .addPrompt()

        createInputPrompt()
            .name("marathonId")
            .message("Marathon id from oengus (eg uksggre25): ")
            .addPrompt()

        // TODO: split between tracker short and oengus marathon id
    }

    terminal.writer().flush()
    terminal.writer().println("Fetching schedules......")

    val trackerShort = marathonShortResult["eventShort"]!!.result
    val marathonId = marathonShortResult["marathonId"]!!.result
    val schedules = oengus.fetchSchedules(marathonId).filter { it.published }

    if (schedules.isEmpty()) {
        prompt.close()

        println("No schedules found, did you publish one?")

        return null
    }

    val schedulesResult = prompt.runPrompt {
        createListPrompt()
            .name("schedule")
            .message("Select your schedule")
            .apply {
                schedules.forEach { schedule ->
                    newItem(schedule.slug).text("${schedule.name} (${schedule.slug})").add()
                }
            }
            .addPrompt()

        createConfirmPromp()
            .name("paddHour")
            .message("Add an hour padding to the last run as a buffer")
            .defaultValue(ConfirmChoice.ConfirmationValue.YES)
            .addPrompt()
    }

    val scheduleSlug = schedulesResult["schedule"]!!.result
    val paddHour = schedulesResult["paddHour"]!!.result == "YES"

    prompt.close()

    println("Schedule selected, copying data")

    println(scheduleSlug)
    println(paddHour)

    return ScheduleFetchSettings(
        trackerShort,
        marathonId,
        scheduleSlug,
        paddHour
    )
}

private fun startImport(settings: Settings, scheduleFetchSettings: ScheduleFetchSettings) {}

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
            } else {
                println("Using stored settings from config.json")
                setupTracker(storedSettings)
            }

            setupOengus(storedSettings)

            val data = askEventQuestions(terminal) ?: return
        }
}
