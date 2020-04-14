package com.centurylink.biwf.model

data class Account(
    var fullName: String = "",
    var streetAddress: String = "",
    var city: String ="",
    var state:String ="",
    var zipcode: String ="",
    var cellNumber: String = "",
    var homeNumber: String = "",
    var workNumber: String = "",
    var emailAddress: String = "",
    var billingAddress: String = ""
)