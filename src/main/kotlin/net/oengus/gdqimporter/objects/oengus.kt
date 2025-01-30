package net.oengus.gdqimporter.objects

data class OSchedule(val name: String, val lines: List<OLine>)

data class OLine(val runner: String, val time: String)
