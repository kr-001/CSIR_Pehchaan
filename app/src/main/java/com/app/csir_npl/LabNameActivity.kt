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
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class LabNameActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SELECTED_LAB_NAME = "com.app.csir_npl.SELECTED_LAB_NAME"
    }

    private lateinit var labListView: ListView
    private lateinit var labListAdapter: LabListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab_name)

        // Initialize views
        labListView = findViewById(R.id.labListView)

        // Create the adapter for the lab names list
        labListAdapter = LabListAdapter(this, getLabNames())
        labListView.adapter = labListAdapter
    }


    private fun getLabNames(): List<String> {
        return listOf(
            "CSIR-Advanced Materials and Processes Research Institute (CSIR-AMPRI), Bhopal",
            "CSIR-Central Building Research Institute(CSIR-CBRI), Roorkee",
            "CSIR-Centre for Cellular Molecular Biology(CSIR-CCMB), Hyderabad",
            "CSIR-Central Drug Research Institute(CSIR-CDRI), Lucknow",
            "CSIR-Central Electrochemical Research Institute(CSIR-CECRI), Karaikudi",
            "CSIR-Central Electronics Engineering Research Institute(CSIR-CEERI), Pilani",
            "CSIR-Central Food Technological Research Institute(CSIR-CFTRI), Mysore",
            "CSIR-Central Glass Ceramic Research Institute(CSIR-CGCRI), Kolkata",
            "CSIR-Central Institute of Medicinal Aromatic Plants(CSIR-CIMAP), Lucknow",
            "CSIR-Central Institute of Mining and Fuel Research(CSIR-CIMFR) Dhanbad",
            "CSIR-Central Leather Research Institute(CSIR-CLRI), Chennai",
            "CSIR-Central Mechanical Engineering Research Institute(CSIR-CMERI), Durgapur",
            "CSIR-Central Road Research Institute(CSIR-CRRI), New Delhi",
            "CSIR-Central Scientific Instruments Organisation(CSIR-CSIO), Chandigarh",
            "CSIR-Central Salt Marine Chemicals Research Institute(CSIR-CSMCRI), Bhavnagar",
            "CSIR Fourth Paradigm Institute(CSIR-4PI), Bengaluru",
            "CSIR-Institute of Genomics and Integrative Biology(CSIR-IGIB), Delhi",
            "CSIR-Institute of Himalayan Bioresource Technology(CSIR-IHBT), Palampur",
            "CSIR-Indian Institute of Chemical Biology(CSIR-IICB), Kolkata",
            "CSIR-Indian Institute of Chemical Technology(CSIR-IICT), Hyderabad",
            "CSIR-Indian Institute of Integrative Medicine(CSIR-IIIM), UT of J&K",
            "CSIR-Indian Institute of Petroleum(CSIR-IIP), Dehradun",
            "CSIR-Indian Institute of Toxicology Research(CSIR-IITR), Lucknow",
            "CSIR-Institute of Minerals and Materials Technology(CSIR-IMMT), Bhubaneswar",
            "CSIR-Institute of Microbial Technology(CSIR-IMTECH), Chandigarh",
            "CSIR-National Aerospace Laboratories(CSIR-NAL), Bengaluru",
            "CSIR-National Botanical Research Institute(CSIR-NBRI), Lucknow",
            "CSIR-National Chemical Laboratory(CSIR-NCL), Pune",
            "CSIR-National Environmental Engineering Research Institute(CSIR-NEERI), Nagpur",
            "CSIR-North - East Institute of Science and Technology(CSIR-NEIST), Jorhat",
            "CSIR-National Geophysical Research Institute(CSIR-NGRI), Hyderabad",
            "CSIR-National Institute For Interdisciplinary Science and Technology(CSIR-NIIST),Thiruvananthapuram",
            "CSIR-National Institute of Oceanography(CSIR-NIO), Goa",
            "CSIR-National Metallurgical Laboratory(CSIR-NML), Jamshedpur",
            "CSIR-National Physical Laboratory(CSIR-NPL), New Delhi",
            "CSIR-National Institute of Science Communication & Policy Research(CSIR-NIScPR),New Delhi(Merger of CSIR-NISCAIR & CSIR-NISTADS)",
            "CSIR Madras Complex(CSIR-CMC),Chennai",
            "CSIR-Structural Engineering Research Centre(CSIR-SERC), Chennai"
        )
    }

    inner class LabListAdapter(context: Context, labs: List<String>) :
        ArrayAdapter<String>(context, R.layout.item_lab, labs) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var itemView = convertView
            val viewHolder: ViewHolder

            if (itemView == null) {
                itemView =
                    LayoutInflater.from(context).inflate(R.layout.item_lab, parent, false)
                viewHolder = ViewHolder(itemView)
                itemView.tag = viewHolder
            } else {
                viewHolder = itemView.tag as ViewHolder
            }

            val labName = getItem(position)

            viewHolder.labNameTextView.text = labName

            return itemView!!
        }

        inner class ViewHolder(view: View) {
            val labNameTextView: TextView = view.findViewById(R.id.labNameTextView)
        }
    }
}
