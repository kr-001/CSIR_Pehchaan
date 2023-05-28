package com.app.csir_npl

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class LabNameActivity : AppCompatActivity() {

    private lateinit var labListView: ListView
    private lateinit var labListAdapter: LabListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab_name)

        // Initialize views
        labListView = findViewById(R.id.labListView)

        // Create the adapter for the lab names list
        labListAdapter = LabListAdapter(this, ArrayList())
        labListView.adapter = labListAdapter

        // Retrieve lab names from the database
        retrieveLabNamesFromDatabase()
    }

    private fun retrieveLabNamesFromDatabase() {
        // Database connection details
        val url = "jdbc:sqlserver://plasma-moment-388114:us-central1:ravikumar;databaseName=csir_pehchaan"
        val username = "ravikumar"
        val password = "Ravi@1998"

        var connection: Connection? = null
        var statement: Statement? = null
        var resultSet: ResultSet? = null

        try {
            // Establish the database connection
            connection = DriverManager.getConnection(url, username, password)

            // Create the statement
            statement = connection.createStatement()

            // Execute the query to retrieve lab names
            val query = "SELECT lab_name FROM labs"
            resultSet = statement.executeQuery(query)

            // Process the result set and add lab names to the adapter
            while (resultSet.next()) {
                val labName = resultSet.getString("lab_name")
                labListAdapter.add(labName)
            }

        } catch (e: SQLException) {
            e.printStackTrace()
            Toast.makeText(this, "Error retrieving lab names", Toast.LENGTH_SHORT).show()
        } finally {
            // Close the result set, statement, and connection
            resultSet?.close()
            statement?.close()
            connection?.close()
        }
    }

    companion object {
        const val EXTRA_SELECTED_LAB_NAME = "selected_lab_name"
    }

    class LabListAdapter(context: Context, labs: List<String>) : ArrayAdapter<String>(context, 0, labs) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            val viewHolder: ViewHolder

            if (itemView == null) {
                itemView = LayoutInflater.from(context).inflate(R.layout.activity_lab_name, parent, false)
                viewHolder = ViewHolder(itemView)
                itemView.tag = viewHolder
            } else {
                viewHolder = itemView.tag as ViewHolder
            }

            val labName = getItem(position)

            viewHolder.labNameTextView.text = labName

            return itemView!!
        }

        private class ViewHolder(view: View) {
            val labNameTextView: TextView = view.findViewById(R.id.labNameTextView)
        }
    }
}
