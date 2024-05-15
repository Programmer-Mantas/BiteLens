package com.example.bitelens

data class UserProfile(
    var name: String = "",
    var email: String = "",
    var age: Int = 0,
    var gender: String = "",
    var avatarUri: String = "" // Store URI as a String to save in Firebase
)
