package com.example.orderfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    private var key = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        key = intent.getStringExtra("key").toString()

        val dbRef = FirebaseDatabase.getInstance().getReference("Customer/$key")
        dbRef.get().addOnSuccessListener {
            edtName.setText(it.child("fullName").value as String)
            edtEmail.setText(it.child("email").value as String)
            edtPhoneNumber.setText(it.child("phoneNumber").value as String)
            edtGender.setText(it.child("gender").value as String)
            edtDateOfBirth.setText(it.child("dateOfBirth").value as String)
        }

        back_layout.setOnClickListener() {
            finish()
        }
    }
}