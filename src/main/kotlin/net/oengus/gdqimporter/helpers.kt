package net.oengus.gdqimporter

import java.time.Duration

fun String.durationToGDQ(): String {
    val dur = Duration.parse(this)
    val hours = dur.toHoursPart().padZero()
    val minutes = dur.toMinutesPart().padZero()
    val seconds = dur.toSecondsPart().padZero()


    return "$hours:$minutes:$seconds"
}

fun Int.padZero(): String {
    return if (this < 10) "0$this" else this.toString()
}
