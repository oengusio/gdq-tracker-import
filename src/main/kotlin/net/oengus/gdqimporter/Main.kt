package net.oengus.gdqimporter

import org.jline.consoleui.prompt.ConsolePrompt
import org.jline.consoleui.prompt.PromptResultItemIF
import org.jline.consoleui.prompt.builder.PromptBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder

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

// TODO: if these settings are set we can ignore them
private fun askSetupQuestions(terminal: Terminal) {
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

        // todo: login to tracker
    }

    println("\nresult=$resultMap")

    // TODO: write code

    println("Settings stored in config.json. You can edit this later")
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
            askSetupQuestions(terminal)
            askEventQuestions(terminal)
        }
}
