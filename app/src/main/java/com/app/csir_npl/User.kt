package com.app.csir_npl

import java.io.Serializable

data class User(
    var title: String = "",
    var fullName: String = "",
    var designation: String = "",
    var divisionName: String = "",
    var labName: String = "",
    var cityState: String = "",
    var idCardNumber: String = "",
    var photoPath: String = "",
    var password: String = "",
    var email : String = "",
    var contact : String = ""
) : Serializable
