import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_FULL_NAME = "full_name"
        private const val COLUMN_DESIGNATION = "designation"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_MOBILE = "mobile"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_USERS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_FULL_NAME TEXT," +
                "$COLUMN_DESIGNATION TEXT," +
                "$COLUMN_EMAIL TEXT," +
                "$COLUMN_MOBILE TEXT," +
                "$COLUMN_ADDRESS TEXT," +
                "$COLUMN_PASSWORD TEXT)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun insertUserDetails(
        fullName: String,
        designation: String,
        email: String,
        mobile: String,
        address: String,
        password: String
    ): Long {
        val values = ContentValues()
        values.put(COLUMN_FULL_NAME, fullName)
        values.put(COLUMN_DESIGNATION, designation)
        values.put(COLUMN_EMAIL, email)
        values.put(COLUMN_MOBILE, mobile)
        values.put(COLUMN_ADDRESS, address)
        values.put(COLUMN_PASSWORD, password)

        val db = this.writableDatabase
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun authenticateUser(email: String, password: String): Boolean {
        val selectQuery =
            "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = '$email' AND $COLUMN_PASSWORD = '$password'"

        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)
        val rowCount = cursor?.count ?: 0
        cursor?.close()
        db.close()
        return rowCount > 0
    }
}
