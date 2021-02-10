import database.initDatabase
import server.initHMACGenerator

fun initServices() {
    initHMACGenerator()
    initDatabase()
}