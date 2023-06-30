import com.app.csir_npl.DatabaseHelper
import com.app.csir_npl.User
import com.app.csir_npl.UserDatabaseHelper

fun main() {
    val dbHelper = DatabaseHelper()
    val userDatabaseHelper = UserDatabaseHelper(dbHelper)

    // Create a sample user
    val user = User(
        title = "Mr",
        fullName = "John Doe",
        designation = "Engineer",
        divisionName = "Division",
        labName = "Lab",
        cityState = "City, State",
        idCardNumber = "123456789",
        photoPath = "/path/to/photo.jpg",
        password = "password"
    )

    // Save the user to the database
    userDatabaseHelper.saveUserToDatabase(user)
}
