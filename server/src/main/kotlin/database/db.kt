package database

import DB_URL
import DB_USER
import DB_PASSWORD

import exeptions.*
import server.AntiCSRFToken_SIZE
import server.SSID_LENGTH
import server.toHashPassword
import java.lang.IllegalArgumentException

import java.sql.*

// TODO: 10.12.2020 optimize db queries

// TODO: 16.12.2020 fix delete account/log out

val db: Database by lazy { Database() }

const val USER_TABLE = "users"

// pair name to is unique
val USER_ATTRS = hashSetOf(
    "id" to true,
    "name" to true,
    "email" to true,
    "password" to false,
    "ssid" to true
)

const val SESSION_TABLE = "session"

val SESSION_ATTRS = hashSetOf(
    "ssid" to true,
    "antiCSRFToken" to true,
)

const val CALENDAR_TABLE = "calendar"

val CALENDAR_ATTRS = hashSetOf(
    "id" to true,
    "date" to false,
)

val TABLE_ATTRS = hashMapOf(
    USER_TABLE to USER_ATTRS,
    SESSION_TABLE to SESSION_ATTRS,
    CALENDAR_TABLE to CALENDAR_ATTRS,
)

const val ADD_SESSION_TG = "add_session_row"
const val DELETE_SESSION_UPD_TG = "delete_session_row_on_update"
const val DELETE_SESSION_DEL_TG = "delete_session_row_on_delete"

fun initDatabase() {
    db.createTables()
    db.createFunctions()
    db.createTriggers()
}

class Database {
    private val conn: Connection by lazy {
        DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)
    }


    private val tables = arrayOf(
        """
        CREATE TABLE IF NOT EXISTS $USER_TABLE (
            id integer PRIMARY KEY,
            name varchar(25) UNIQUE,
            email varchar(128) UNIQUE,
            password varchar(256) NOT NULL,
            ssid varchar($SSID_LENGTH)
        );
        """,
        """
        CREATE TABLE IF NOT EXISTS $SESSION_TABLE (
            ssid varchar($SSID_LENGTH) PRIMARY KEY,
            antiCSRFToken varchar($AntiCSRFToken_SIZE)
        );
        """,
        """
        CREATE TABLE IF NOT EXISTS $CALENDAR_TABLE (
            id integer,
            date varchar(11),
            CONSTRAINT unique_record UNIQUE (id, date), 
            CONSTRAINT calendar_fk_id 
                FOREIGN KEY (id) 
                    REFERENCES $USER_TABLE (id) ON DELETE CASCADE 
        );
        """
    )

    private val functions = arrayOf(
        """
        CREATE OR REPLACE FUNCTION ${ADD_SESSION_TG}_fun()
            RETURNS TRIGGER 
            LANGUAGE PLPGSQL 
            AS
            $$
            BEGIN
                IF OLD.ssid IS NULL AND NEW.ssid IS NOT NULL THEN
                    INSERT INTO $SESSION_TABLE (ssid) VALUES (NEW.ssid);
                END IF;
                RETURN NEW;
            END;
            $$;
        """,
        """
        CREATE OR REPLACE FUNCTION ${DELETE_SESSION_UPD_TG}_fun()
            RETURNS TRIGGER 
            LANGUAGE PLPGSQL 
            AS
            $$
            BEGIN
                IF OLD.ssid IS NOT NULL AND NEW.ssid IS NULL THEN
                    DELETE FROM $SESSION_TABLE WHERE ssid = OLD.ssid;
                END IF;
                RETURN NEW;
            END;
            $$;
        """,
        """
        CREATE OR REPLACE FUNCTION ${DELETE_SESSION_DEL_TG}_fun()
            RETURNS TRIGGER 
            LANGUAGE PLPGSQL 
            AS
            $$
            BEGIN
                IF OLD.ssid IS NOT NULL THEN
                    DELETE FROM $SESSION_TABLE WHERE ssid = OLD.ssid;
                END IF;
                RETURN NEW;
            END;
            $$;
        """,
    )

    private val triggers = arrayOf(
        """
        DROP TRIGGER IF EXISTS $ADD_SESSION_TG ON $USER_TABLE CASCADE;
        CREATE TRIGGER $ADD_SESSION_TG
            AFTER UPDATE OF ssid
            ON $USER_TABLE
            FOR EACH ROW
                EXECUTE PROCEDURE ${ADD_SESSION_TG}_fun();
        """,
        """
        DROP TRIGGER IF EXISTS $DELETE_SESSION_UPD_TG ON $USER_TABLE CASCADE;
        CREATE TRIGGER $DELETE_SESSION_UPD_TG
            AFTER UPDATE OF ssid 
            ON $USER_TABLE
            FOR EACH ROW
                EXECUTE PROCEDURE ${DELETE_SESSION_UPD_TG}_fun();
        """,
        """
        DROP TRIGGER IF EXISTS $DELETE_SESSION_DEL_TG ON $USER_TABLE CASCADE;
        CREATE TRIGGER $DELETE_SESSION_DEL_TG
            AFTER DELETE
            ON $USER_TABLE
            FOR EACH ROW
                EXECUTE PROCEDURE ${DELETE_SESSION_DEL_TG}_fun();
        """,
    )

    init {
        try {
            conn.clientInfo
            println("Connection to SQLite has been established.")
        } catch (e: SQLException) {
            throw DatabaseConnectionException(DB_URL, e.message)
        }
    }

    fun createTables() = tables.forEach { query(it) }
    fun createFunctions() = functions.forEach { query(it) }
    fun createTriggers() = triggers.forEach { query(it) }

    private fun query(query: String) {
        try {
            conn.createStatement().execute(query)
        } catch (e: SQLException) {
            throw DatabaseQueryException(query, e.message)
        }
    }

    private fun checkValidFields(table: String, vararg attrs: Pair<String, Boolean>) {
        val tableAttrs =
            TABLE_ATTRS[table] ?: throw DatabaseInvalidQueryParametersException("checkValidField", "table:$table")
        attrs.map {
            tableAttrs.contains(it) || if (!it.second) tableAttrs.contains(it.first to true) else false
        }.reduce { acc, b -> acc && b }.let {
            if (!it) throw DatabaseInvalidQueryParametersException("checkValidField", "attrs:$attrs")
        }
    }

    fun addUser(name: String, email: String, password: String): Int {
        val stmt = conn.prepareStatement(
            "INSERT INTO $USER_TABLE (id, name, email, password, ssid) VALUES (?, ?, ?, ?, NULL);"
        )
        val id = generateUniqueId().also { stmt.setInt(1, it) }
        stmt.setString(2, name)
        stmt.setString(3, email)
        stmt.setString(4, password)
        try {
            stmt.execute()
        } catch (e: SQLException) {
            throw DatabaseQueryInsertException("addUser")
        }
        return id
    }

    fun deleteUser(ssid: String) {
        val stmt = conn.prepareStatement(
            "DELETE FROM $USER_TABLE WHERE ssid = ?;"
        )
        stmt.setString(1, ssid)
        if (stmt.executeUpdate() == 0) throw DatabaseQueryException("DELETE FROM $USER_TABLE...", "- 0 rows affected")
    }

    private fun generateUniqueId(): Int {
        var id: Int
        do {
            id = util.generateUniqueUserId()
        } while (!isUniqueIdUser(id))
        return id
    }

    private fun intSetter(ind: Int): (Int?) -> (PreparedStatement.() -> Unit) {
        return { it?.let { value -> { setInt(ind, value) } } ?: { setNull(ind, Types.INTEGER) } }
    }

    private fun stringSetter(ind: Int): (String?) -> (PreparedStatement.() -> Unit) {
        return { it?.let { value -> { setString(ind, value) } } ?: { setNull(ind, Types.VARCHAR) } }
    }

    private fun setAttr(
        table: String,
        attr: String,
        value: String?,
        byAttr: String,
        setter: PreparedStatement.() -> Unit
    ): Int {
        checkValidFields(table, attr to false, byAttr to true)
        val stmt = conn.prepareStatement("UPDATE $table SET $attr = ? WHERE $byAttr = ?;")
        value?.let { stmt.setString(1, it) } ?: stmt.setNull(1, Types.VARCHAR)
        stmt.setter()
        return stmt.executeUpdate()
    }

    private fun setAttrNotZero(
        table: String,
        attr: String,
        value: String?,
        byAttr: String,
        setter: PreparedStatement.() -> Unit
    ) = setAttr(table, attr, value, byAttr, setter).let {
        if (it == 0) throw DatabaseQueryUpdateException("setAttrNotZero")
    }

    private fun setUserAttr(attr: String, value: String?, byAttr: String, setter: PreparedStatement.() -> Unit) =
        setAttrNotZero(USER_TABLE, attr, value, byAttr, setter)

    private fun setSessionAttr(attr: String, value: String?, byAttr: String, setter: PreparedStatement.() -> Unit) =
        setAttrNotZero(SESSION_TABLE, attr, value, byAttr, setter)

    fun setUserAttrById(attr: String, value: String, id: Int) =
        setUserAttr(attr, value, "id", intSetter(2)(id))

    fun setUserAttrByString(attr: String, newValue: String?, byAttr: String, value: String) =
        setUserAttr(attr, newValue, byAttr, stringSetter(2)(value))

    private fun setSessionAttrBySSID(attr: String, value: String, ssid: String) =
        setSessionAttr(attr, value, "ssid", stringSetter(2)(ssid))

    fun setAntiCSRFToken(token: String, ssid: String) = setSessionAttrBySSID("antiCSRFToken", token, ssid)

    private fun selectByField(
        table: String,
        attr: String,
        setter: PreparedStatement.() -> Unit
    ): ResultSet {
        checkValidFields(table, attr to false)
        val stmt = conn.prepareStatement("SELECT * FROM $table WHERE $attr = ?;")
        stmt.setter()
        return stmt.executeQuery()
    }

    private fun isUniqueField(
        table: String,
        attr: String,
        setter: PreparedStatement.() -> Unit
    ): Boolean = !selectByField(table, attr, setter).next()

    private fun isUniqueStringField(table: String, attr: String, value: String) =
        isUniqueField(table, attr) { setString(1, value) }

    private fun isUniqueIntField(table: String, attr: String, value: Int) =
        isUniqueField(table, attr) { setInt(1, value) }

    private fun isUniqueIntFieldUser(attr: String, value: Int) = isUniqueIntField(USER_TABLE, attr, value)
    private fun isUniqueStringFieldUser(attr: String, value: String) = isUniqueStringField(USER_TABLE, attr, value)

    private fun isUniqueIdUser(value: Int) = isUniqueIntFieldUser("id", value)

    fun isUniqueEmailUser(value: String) = isUniqueStringFieldUser("email", value)
    fun isUniqueNameUser(value: String) = isUniqueStringFieldUser("name", value)

    fun isUniqueSession(ssid: String) = isUniqueStringField(SESSION_TABLE, "ssid", ssid)

    private fun getField(
        setter: PreparedStatement.() -> Unit,
    ) = { table: String,
          byAttr: String,
          field: String
        ->
        checkValidFields(table, byAttr to true, field to false)
        selectByField(table, byAttr, setter).apply { next() }.run { if (!isClosed) getObject(field) else null }
    }

    private fun <T> getFromUser(
        byAttr: String,
        value: T,
        field: String,
        getSetter: (T) -> (PreparedStatement.() -> Unit),
    ): Any? = getField(getSetter(value))(USER_TABLE, byAttr, field)

    fun getFromUserById(
        id: Int,
        field: String
    ) = getFromUser("id", id, field, intSetter(1))

    fun getFromUserByString(
        byAttr: String,
        value: String,
        field: String
    ) = getFromUser(byAttr, value, field, stringSetter(1))

    private fun getSessionBySSID(
        ssid: String,
        field: String
    ) = getField(stringSetter(1)(ssid))(SESSION_TABLE, "ssid", field)

    fun getAntiCSRFToken(ssid: String) = getSessionBySSID(ssid, "antiCSRFToken") as String?

    private fun affectDate(sql: String, ssid: String, date: String) {
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, ssid)
        stmt.setString(2, date)
        stmt.execute()
    }

    fun addDate(ssid: String, date: String) = affectDate(
        sql = "INSERT INTO $CALENDAR_TABLE (id, date) VALUES ((SELECT id FROM $USER_TABLE WHERE ssid = ?), ?);",
        ssid = ssid, date = date,
    )

    fun deleteDate(ssid: String, date: String) = affectDate(
        sql = "DELETE FROM $CALENDAR_TABLE WHERE id = (SELECT id FROM $USER_TABLE WHERE ssid = ?) AND date = ?;",
        ssid = ssid, date = date,
    )

    fun getDates(ssid: String): List<String> {
        val retval = mutableListOf<String>()
        val stmt = conn.prepareStatement(
            "SELECT date FROM $CALENDAR_TABLE WHERE id = (SELECT id FROM $USER_TABLE WHERE ssid = ?);"
        )
        stmt.setString(1, ssid)
        val resultSet = stmt.executeQuery()
        while (resultSet.next()) {
            retval.add(resultSet.getString("date"))
        }
        return retval
    }
}

fun addUser(name: String, email: String, password: String): String {
    return db.addUser(name, email, password).toString()
}

fun isUniqueName(name: String): Boolean {
    return db.isUniqueNameUser(name)
}

fun isUniqueEmail(email: String): Boolean {
    return db.isUniqueEmailUser(email)
}

fun setAntiCSRFToken(ssid: String, token: String) {
    db.setAntiCSRFToken(token, ssid)
}

fun getAntiCSRFToken(ssid: String): String? {
    return db.getAntiCSRFToken(ssid)
}

fun setSSID(userId: Int, ssid: String) {
    db.setUserAttrById("ssid", ssid, userId)
}

fun validSSID(ssid: String): Boolean {
    return !db.isUniqueSession(ssid)
}

fun getUserAttr(ssid: String, attr: String): Any? {
    return db.getFromUserByString("ssid", ssid, attr)
}

fun getUserAttrByEmail(email: String, attr: String): Any? {
    return db.getFromUserByString("email", email, attr)
}

fun getUserAttrsByEmail(email: String, vararg attrs: String): Map<String, Any?> {
    return attrs.map { it to getUserAttrByEmail(email, it) }.toMap()
}

fun getUserAttrs(ssid: String, vararg attrs: String): Map<String, Any?> {
    return attrs.map { it to getUserAttr(ssid, it) }.toMap()
}

fun setPassword(ssid: String, password: String) {
    val hash = toHashPassword(password)
    // TODO: 12.12.2020 invalidate other sessions
    db.setUserAttrByString("password", hash, "ssid", ssid)
}

fun setEmail(ssid: String, email: String) {
    db.setUserAttrByString("email", email, "ssid", ssid)
}

fun setAccountName(ssid: String, name: String) {
    db.setUserAttrByString("name", name, "ssid", ssid)
}

fun deleteAccount(ssid: String) {
    db.deleteUser(ssid)
}

fun closeSession(ssid: String) {
    db.setUserAttrByString("ssid", null, "ssid", ssid)
}

fun changeDate(ssid: String, date: String, state: Boolean): Boolean {
    return server.validateDate(date).also {
        if (it) {
            if (state) db.addDate(ssid, date) else db.deleteDate(ssid, date)
        }
    }
}

fun getDates(ssid: String): List<String> {
    return db.getDates(ssid)
}

fun smartCast(field: String, value: Any?): String? {
    return when (field) {
        "name" -> value as String?
        "email" -> value as String?
        "password" -> value as String?
        "ssid" -> value as String?
        "id" -> (value as Int?).toString()
        else -> throw IllegalArgumentException("illegal field")
    }
}