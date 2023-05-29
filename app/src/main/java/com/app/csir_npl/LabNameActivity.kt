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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val hostname = "34.29.155.1"
        val port = 1433 // Replace with the actual port number of your SQL Server instance
        val databaseName = "csir_npl"
        val username = "sqlserver"
        val password = "sql123"
        val jdbcUrl = "jdbc:sqlserver://$hostname:$port;database=$databaseName;user=$username;password=$password"

        GlobalScope.launch(Dispatchers.IO) {
            var connection: Connection? = null
            var statement: Statement? = null
            var resultSet: ResultSet? = null

            try {
                connection = DriverManager.getConnection(jdbcUrl, username, password)
                statement = connection.createStatement()
                val query = "SELECT name FROM CSIR_Labs"
                resultSet = statement.executeQuery(query)

                withContext(Dispatchers.Main) {
                    labListAdapter.clear()
                    while (resultSet.next()) {
                        val labName = resultSet.getString("name")
                        labListAdapter.add(labName)
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LabNameActivity, "Error retrieving lab names", Toast.LENGTH_SHORT).show()
                }
            } finally {
                resultSet?.close()
                statement?.close()
                connection?.close()
            }
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
