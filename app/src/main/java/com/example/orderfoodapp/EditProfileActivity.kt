package com.example.orderfoodapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.lang.Exception

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

        edit_button.setOnClickListener() {
            btnUpdate.visibility = View.VISIBLE
        }

        btnUpdate.setOnClickListener() {
            try {
                val dbUpdate = FirebaseDatabase.getInstance().getReference("Customer/$key")
                dbUpdate.child("fullName").setValue(edtName.text.toString())
                dbUpdate.child("phoneNumber").setValue(edtPhoneNumber.text.toString())
                dbUpdate.child("gender").setValue(edtGender.text.toString())
                dbUpdate.child("dateOfBirth").setValue(edtDateOfBirth.text.toString())

                btnUpdate.visibility = View.GONE
                Toast.makeText(this, "Update successfully!", Toast.LENGTH_LONG).show()
            }
            catch (e: Exception) {
                Toast.makeText(this, "Update failed! Please try again!", Toast.LENGTH_LONG).show()
            }
        }
    }
}