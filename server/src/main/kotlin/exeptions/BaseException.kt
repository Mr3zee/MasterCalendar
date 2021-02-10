package exeptions

import java.util.*

abstract class BaseException: RuntimeException() {
    abstract val exceptionName: String
    abstract val exceptionDescription: String

    override val message: String
        get() = "\n* * * ${getTime()} >>> SERVER FATAL ERROR >>> $exceptionName\n" +
                "* * * Exception description >>> $exceptionDescription"

    private fun getTime(): String {
        return Date().toString()
    }
}