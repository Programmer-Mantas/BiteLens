//package com.example.bitelens
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseExpandableListAdapter
//import android.widget.ListAdapter
//import android.widget.TextView
//
//
//class FoodExpandableListAdapter(
//    private val context: Context,
//    private val dateGroups: List<DateGroup>
//) : BaseExpandableListAdapter(), ListAdapter {
//
//    override fun getGroupCount(): Int {
//        return dateGroups.size
//    }
//
//    override fun getChildrenCount(groupPosition: Int): Int {
//        return dateGroups[groupPosition].items.size
//    }
//
//    override fun getGroup(groupPosition: Int): Any {
//        return dateGroups[groupPosition]
//    }
//
//    override fun getChild(groupPosition: Int, childPosition: Int): Any {
//        return dateGroups[groupPosition].items[childPosition]
//    }
//
//    override fun getGroupId(groupPosition: Int): Long {
//        return groupPosition.toLong()
//    }
//
//    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
//        return childPosition.toLong()
//    }
//
//    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
//        return true // allows each item to be selectable
//    }
//
//    override fun getCount(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getItem(p0: Int): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun getItemId(p0: Int): Long {
//        TODO("Not yet implemented")
//    }
//
//    override fun hasStableIds(): Boolean {
//        return true // as ids are consistent even after a change to the data
//    }
//
//    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
//        TODO("Not yet implemented")
//    }
//
//    override fun getItemViewType(p0: Int): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getViewTypeCount(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun isEnabled(p0: Int): Boolean {
//        TODO("Not yet implemented")
//    }
//
//    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
//        val group = getGroup(groupPosition) as DateGroup
//        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.group_item_layout, parent, false)
//        val textView: TextView = view.findViewById(R.id.groupNameTextView)
//        textView.text = group.name
//        return view
//    }
//
//
//    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
//                              convertView: View?, parent: ViewGroup?): View {
//        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.food_item, parent, false)
//        val dateTextView: TextView = view.findViewById(R.id.foodDateTextView)
//        val caloriesTextView: TextView = view.findViewById(R.id.foodCaloriesTextView)
//        val servingTextView: TextView = view.findViewById(R.id.foodServingSizeTextView)
//        val fatTotalTextView: TextView = view.findViewById(R.id.foodFatTotalTextView)
//        val fatSTextView: TextView = view.findViewById(R.id.foodFatSaturatedTextView)
//        val proteinTextView: TextView = view.findViewById(R.id.foodProteinTextView)
//        val sodiumTextView: TextView = view.findViewById(R.id.foodSodiumTextView)
//        val potassiumTextView: TextView = view.findViewById(R.id.foodPotassiumTextView)
//        val cholesterolTextView: TextView = view.findViewById(R.id.foodCholesterolTextView)
//        val carboTextView: TextView = view.findViewById(R.id.foodCarbohydratesTextView)
//        val fiberTextView: TextView = view.findViewById(R.id.foodFiberTextView)
//        val sugarTextView: TextView = view.findViewById(R.id.foodSugarTextView)
//        val child = getChild(groupPosition, childPosition) as FoodData
//        dateTextView.text = child.date
//        caloriesTextView.text = child.calories.toString()
//        servingTextView.text = child.servingSizeG.toString()
//        fatTotalTextView.text = child.fatTotalG.toString()
//        fatSTextView.text = child.fatSaturatedG.toString()
//        proteinTextView.text = child.proteinG.toString()
//        sodiumTextView.text = child.sodiumMg.toString()
//        potassiumTextView.text = child.potassiumMg.toString()
//        cholesterolTextView.text = child.cholesterolMg.toString()
//        carboTextView.text = child.carbohydratesTotalG.toString()
//        fiberTextView.text = child.fiberG.toString()
//        sugarTextView.text = child.sugarG.toString()
//        return view
//    }
//
//}

package com.example.bitelens

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import java.util.Locale

class FoodExpandableListAdapter(
    private val context: Context,
    private val dateGroups: List<DateGroup>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return dateGroups.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return dateGroups[groupPosition].items.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return dateGroups[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return dateGroups[groupPosition].items[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.group_item_layout, parent, false)
        val groupNameTextView: TextView = view.findViewById(R.id.groupNameTextView)
        groupNameTextView.text = (getGroup(groupPosition) as DateGroup).name
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.food_item, parent, false)

        val foodItem = getChild(groupPosition, childPosition) as FoodData

        // Binding the food data to the TextViews in the layout
        view.findViewById<TextView>(R.id.foodDateTextView).text = foodItem.date
        view.findViewById<TextView>(R.id.foodCaloriesTextView).text = String.format(Locale.US, "%.2f calories", foodItem.calories)
        view.findViewById<TextView>(R.id.foodServingSizeTextView).text = String.format(Locale.US, "%.2f g", foodItem.servingSizeG)
        view.findViewById<TextView>(R.id.foodFatTotalTextView).text = String.format(Locale.US, "%.2f g", foodItem.fatTotalG)
        view.findViewById<TextView>(R.id.foodFatSaturatedTextView).text = String.format(Locale.US, "%.2f g", foodItem.fatSaturatedG)
        view.findViewById<TextView>(R.id.foodProteinTextView).text = String.format(Locale.US, "%.2f g", foodItem.proteinG)
        view.findViewById<TextView>(R.id.foodSodiumTextView).text = String.format(Locale.US, "%.2f mg", foodItem.sodiumMg)
        view.findViewById<TextView>(R.id.foodPotassiumTextView).text = String.format(Locale.US, "%.2f mg", foodItem.potassiumMg)
        view.findViewById<TextView>(R.id.foodCholesterolTextView).text = String.format(Locale.US, "%.2f mg", foodItem.cholesterolMg)
        view.findViewById<TextView>(R.id.foodCarbohydratesTextView).text = String.format(Locale.US, "%.2f g", foodItem.carbohydratesTotalG)
        view.findViewById<TextView>(R.id.foodFiberTextView).text = String.format(Locale.US, "%.2f g", foodItem.fiberG)
        view.findViewById<TextView>(R.id.foodSugarTextView).text = String.format(Locale.US, "%.2f g", foodItem.sugarG)

        return view
    }


    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}
