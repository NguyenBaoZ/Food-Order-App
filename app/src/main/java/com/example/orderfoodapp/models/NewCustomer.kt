package com.example.orderfoodapp.models

data class NewCustomer(
    var address: String,
    var balance: Long,
    var dateOfBirth: String,
    var email: String,
    var fullName: String,
    var gender: String,
    var phoneNumber: String
)