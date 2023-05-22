package com.app.csir_npl

data class User(
    val id: String, // Add the id property
    val fullName: String,
    val designation: String,
    val email: String,
    val mobile: String,
    val address: String,
    val password: String,
    val confirmPassword: String,
    val duName: String,
    val dpName: String,
    var profilePictureUrl: String // Add the profilePictureUrl property
)