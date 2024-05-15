package com.example.bitelens

data class FoodData(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val time: String = "",
    val name: String = "",
    var calories: Double = 0.0,
    val servingSizeG: Double = 0.0,
    val fatTotalG: Double = 0.0,
    val fatSaturatedG: Double = 0.0,
    val proteinG: Double = 0.0,
    val sodiumMg: Double = 0.0,
    val potassiumMg: Double = 0.0,
    val cholesterolMg: Double = 0.0,
    val carbohydratesTotalG: Double = 0.0,
    val fiberG: Double = 0.0,
    val sugarG: Double = 0.0
)


