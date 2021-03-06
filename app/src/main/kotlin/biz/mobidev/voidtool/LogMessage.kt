package biz.mobidev.voidtool

import org.intellij.lang.annotations.Language

/*
    Level 0 – “Emergency”
    Level 1 – “Alert”
    Level 2 – “Critical”
    Level 3 – “Error”
    Level 4 – “Warning”
    Level 5 – “Notice”
    Level 6 – “Info”
    Level 7 – “Debug”
 */
enum class LogLevel {
    UNKNOWN,
    EMERGENCY,
    ALERT,
    CRITICAL,
    ERROR,
    WARNING,
    NOTICE,
    INFO,
    DEBUG
}

data class LogMessage(
        val logDate: String,
        val deviceName: String,
        val parentProcess: String,
        val logLevelString: String,
        val message: String
) {
    val logLevel: LogLevel = when (logLevelString.trim()) {
        "<Emergency>" -> LogLevel.EMERGENCY
        "<Alert>" -> LogLevel.ALERT
        "<Critical>" -> LogLevel.CRITICAL
        "<Error>" -> LogLevel.ERROR
        "<Warning>" -> LogLevel.WARNING
        "<Notice>" -> LogLevel.NOTICE
        "<Info>" -> LogLevel.INFO
        "<Debug>" -> LogLevel.DEBUG
        "" -> LogLevel.UNKNOWN
        else -> {
            println("Note: unknown log level: $this")
            LogLevel.UNKNOWN
        }
    }

    override fun toString(): String = "$logDate $deviceName $parentProcess $logLevelString: $message\n"

    fun toDetailedString(): String = "$logDate $deviceName $parentProcess $logLevelString:\n" +
            message.split("\n").map { it.trim() }.joinToString("") { "\t| $it\n" }

    fun matchesFilter(filter: String) = filter.isBlank() ||
            deviceName.contains(filter, ignoreCase = true) ||
            message.contains(filter, ignoreCase = true) ||
            parentProcess.contains(filter, ignoreCase = true) ||
            logDate.contains(filter, ignoreCase = true)
}

@Language("RegExp")
private val syslogDate = """\w{3}\s+\d{1,2}\s\d{1,2}:\d{1,2}:\d{1,2}"""
@Language("RegExp")
private val syslogDeviceName = """.*?"""
@Language("RegExp")
private val syslogParentProcess = """.+(?:\(.+\))?\[\d+]"""
@Language("RegExp")
private val syslogLogLevel = """<\w+>"""
@Language("RegExp")
private val syslogMessageText = """.*"""
private val syslogMessageRegex = Regex(
        "($syslogDate)\\s+" +
                "($syslogDeviceName)\\s+" +
                "($syslogParentProcess)\\s+" +
                "($syslogLogLevel):\\s+" +
                "($syslogMessageText)")

fun String.parseLogMessage(): LogMessage? =
        syslogMessageRegex.matchEntire(this)?.let {
            LogMessage(
                    it.groupValues[1],
                    it.groupValues[2],
                    it.groupValues[3],
                    it.groupValues[4],
                    it.groupValues[5])
        }
