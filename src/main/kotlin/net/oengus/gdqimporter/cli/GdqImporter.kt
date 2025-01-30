package net.oengus.gdqimporter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option

class GdqImporter : CliktCommand() {
    val oengusUrl: String by option("--oengus-url")
        .default("https://oengus.io/")
        .help("The Oengus instance to use, E.G. https://sandbox.oengus.io/")

    override fun run() {
        TODO("Not yet implemented")
    }
}
