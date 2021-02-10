package server

import de.mkammerer.argon2.*

val argon2: Argon2 = Argon2Factory.create()

const val MEMORY = 65536
const val THREADS = 1
const val MAX_TIME = 500L

val ITER = Argon2Helper.findIterations(argon2, MAX_TIME, MEMORY, THREADS)

fun toHashPassword(password: String): String {
    return argon2.hash(ITER, MEMORY, THREADS, password.toCharArray())
}

fun verifyPassword(hash: String, password: String): Boolean {
    return argon2.verify(hash, password.toCharArray())
}