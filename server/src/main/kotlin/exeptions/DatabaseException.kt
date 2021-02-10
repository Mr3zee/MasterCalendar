package exeptions

import java.lang.reflect.Parameter

abstract class DatabaseException() : BaseException() {
    override val exceptionName: String
        get() = "Database Exception"
}

class DatabaseConnectionException(
    private val dbUrl: String,
    private val externalMessage: String? = "no message"
) : DatabaseException() {
    override val exceptionDescription: String
        get() = "Connection to database failed, URL: $dbUrl\n" +
                "Message: ${externalMessage ?: "no message"}"

    fun getUrl() = dbUrl
}

open class DatabaseQueryException(
    private val query: String,
    private val externalMessage: String? = "no message"
) : DatabaseException() {
    override val exceptionDescription: String
        get() = "Executing query failed: \n\n$query\n\n" +
                "Message: ${externalMessage ?: "no message"}"
}

class DatabaseQueryUpdateException(place: String) : DatabaseQueryException(
    query = "Updating attr with invalid row id in $place"
)

class DatabaseQueryInsertException(place: String) : DatabaseQueryException(
    query = "Inserting not unique values in $place"
)

class DatabaseInvalidQueryParametersException(place: String, parameter: String) : DatabaseQueryException(
    query = "Invalid parameter requested: $parameter in $place"
)

class DatabaseQuerySelectException(place: String) : DatabaseQueryException(
    query = "No values selected in $place"
)