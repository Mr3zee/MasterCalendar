package util

import java.security.SecureRandom
import kotlin.streams.asSequence

val RANDOM = SecureRandom()

const val UPPER_BOUND = 2e9.toInt()

fun generateUniqueUserId(): Int {
    return RANDOM.nextInt(UPPER_BOUND)
}

val CHAR_POOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun generateRandomHash(length: Long): String {
    return RANDOM.ints(length, 0, CHAR_POOL.size)
        .asSequence().map { CHAR_POOL[it] }.joinToString("")
}