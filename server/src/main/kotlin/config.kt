import exeptions.EvnException


val DB_USER = getEnv("JDBC_DATABASE_USERNAME")
val DB_PASSWORD = getEnv("JDBC_DATABASE_PASSWORD")
val DB_URL = getEnv("JDBC_DATABASE_URL")

val HMAC_SECRET = getEnv("HMAC_SECRET")

fun getEnv(name: String): String {
    return System.getenv(name) ?: throw EvnException(name)
}