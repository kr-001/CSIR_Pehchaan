package com.app.csir_npl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class LabNameAdapter(private val context: Context, private val labNames: MutableList<String>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.custom_spinner_item, parent, false)
        val textView = view.findViewById<TextView>(R.id.lab_name_text)
        textView.text = labNames[position]
        return view
    }

    override fun getItem(position: Int): String {
        return labNames[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return labNames.size
    }
}
