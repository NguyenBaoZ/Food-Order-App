package com.example.orderfoodapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.orderfoodapp.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_fill_information.*

class FillInformationActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_information)

        val gender = resources.getStringArray(R.array.gender)
        val genderAdapter = ArrayAdapter(this, R.layout.item_dropdown_gender, gender)
        gender_inputText.setAdapter(genderAdapter)

        //date picker
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Date of birth:")
        builder.setTheme(R.style.MaterialCalendarTheme)
        val materialDatePicker = builder.build()

        ic_calendar.setOnClickListener() {
            materialDatePicker.show(supportFragmentManager, "Date picker")
        }

        materialDatePicker.addOnPositiveButtonClickListener() {
            dateOFfBirth_inputText.setText(materialDatePicker.headerText)
        }

        confirm_button.setOnClickListener() {
            updateUserData()
        }
    }

    private fun updateUserData() {
        val customerEmail = Firebase.auth.currentUser?.email.toString()

        val name = name_inputText.text.toString()
        val address = address_inputText.text.toString()
        val phoneNumber = phoneNumber_inputText.text.toString()
        val gender = gender_inputText.text.toString()
        val dateOfBirth = dateOFfBirth_inputText.text.toString()

        if(name.isEmpty()) {
            name_inputText.error = "Name is required"
            name_inputText.requestFocus()
        }
        else if(address.isEmpty()) {
            address_inputText.error = "address is required"
            address_inputText.requestFocus()
        }
        else if(phoneNumber.isEmpty()) {
            phoneNumber_inputText.error = "phoneNumber is required"
            phoneNumber_inputText.requestFocus()
        }
        else if(phoneNumber.length != 10) {
            phoneNumber_inputText.error = "invalid phoneNumber"
            phoneNumber_inputText.requestFocus()
        }
        else if(dateOfBirth.isEmpty()) {
            dateOFfBirth_inputText.error = "dateOfBirth is required"
            dateOFfBirth_inputText.requestFocus()
        }
        else {
            val dbRef = FirebaseDatabase.getInstance().getReference("Customer")
            val query = dbRef.orderByChild("email").equalTo(customerEmail)
            query.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        for(data in snapshot.children) {
                            val dbUpdate = FirebaseDatabase.getInstance().getReference("Customer/${data.key}")
                            dbUpdate.child("fullName").setValue(name)
                            dbUpdate.child("address").setValue(address)
                            dbUpdate.child("phoneNumber").setValue(phoneNumber)
                            dbUpdate.child("gender").setValue(gender)
                            dbUpdate.child("dateOfBirth").setValue(dateOfBirth)
                            Toast.makeText(this@FillInformationActivity, "Updated successfully", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }
}