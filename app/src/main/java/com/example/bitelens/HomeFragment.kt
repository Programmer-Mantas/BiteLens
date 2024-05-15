package com.example.bitelens

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ExpandableListView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var expandableListView: ExpandableListView
    private lateinit var adapter: FoodExpandableListAdapter
    private var dateGroups: MutableList<DateGroup> = mutableListOf()
    private var originalDateGroups: MutableList<DateGroup> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        expandableListView = view.findViewById(R.id.expandableListView)
        setupSortingAndFiltering(view)
        queryFoodData()

        return view
    }

    private fun setupSortingAndFiltering(view: View) {
        val buttonSort = view.findViewById<Button>(R.id.buttonSort)
        val editTextSort = view.findViewById<EditText>(R.id.editTextSort)
        buttonSort.setOnClickListener {
//            sortDataByName(editTextSort.text.toString())
            filterDataByName(editTextSort.text.toString())
        }

        val buttonFilter = view.findViewById<Button>(R.id.buttonFilter)
        val editTextFilter = view.findViewById<EditText>(R.id.editTextFilter)
        editTextFilter.setOnClickListener {
            showDatePickerDialog(it as EditText)
        }
        buttonFilter.setOnClickListener {
            filterDataByDate(editTextFilter.text.toString())
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            editText.setText(selectedDate)
        }, year, month, day).show()
    }

//    private fun sortDataByName(name: String) {
//        dateGroups.sortWith(compareByDescending<DateGroup> { it.name.contains(name, ignoreCase = true) }
//            .thenBy { it.name })
//        adapter.notifyDataSetChanged()
//    }
private fun filterDataByName(name: String) {
    if (name.isEmpty()) {
        dateGroups.clear()
        dateGroups.addAll(originalDateGroups) // Restore the original data if filter is cleared
    } else {
        val filteredGroups = mutableListOf<DateGroup>()
        for (group in originalDateGroups) {
            val filteredItems = group.items.filter { it.name.contains(name, ignoreCase = true) }
            if (filteredItems.isNotEmpty()) {
                filteredGroups.add(DateGroup(group.name, group.date, filteredItems.toMutableList()))
            }
        }
        dateGroups.clear()
        dateGroups.addAll(filteredGroups)
    }
    adapter.notifyDataSetChanged()
}

    private fun filterDataByDate(date: String) {
        if (date.isEmpty()) {
            dateGroups.clear()
            dateGroups.addAll(originalDateGroups) // Restore the original data if filter is cleared
        } else {
            val filteredGroups = mutableListOf<DateGroup>()
            for (group in originalDateGroups) {
                val filteredItems = group.items.filter { it.date == date }
                if (filteredItems.isNotEmpty()) {
                    filteredGroups.add(DateGroup(group.name, date, filteredItems.toMutableList()))
                }
            }
            dateGroups.clear()
            dateGroups.addAll(filteredGroups)
        }
        adapter.notifyDataSetChanged()
    }





    private fun queryFoodData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance("https://bitelens-90db4-default-rtdb.europe-west1.firebasedatabase.app").getReference("Foods")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalDateGroups.clear() // Clear the original data list first
                dateGroups.clear() // Clear the current view list

                snapshot.children.forEach { child ->
                    val foodItem = child.getValue(FoodData::class.java)
                    if (foodItem != null && foodItem.userId == currentUserId) {
                        val groupIndex = dateGroups.indexOfFirst { it.name == foodItem.name }
                        if (groupIndex >= 0) {
                            dateGroups[groupIndex].items.add(foodItem)
                        } else {
                            val newGroup = DateGroup(foodItem.name, foodItem.date, mutableListOf(foodItem))
                            dateGroups.add(newGroup)
                            originalDateGroups.add(newGroup.copy()) // Add a copy to preserve original state
                        }
                    }
                }
                adapter = FoodExpandableListAdapter(requireContext(), dateGroups)
                expandableListView.setAdapter(adapter)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle potential errors
            }
        })
    }

}

