package net.oengus.gdqimporter

import kotlinx.serialization.json.Json

const val SETTINGS_FILE_NAME = "config.json"

val json = Json { // this: JsonBuilder
    encodeDefaults = true
    ignoreUnknownKeys = true
}
