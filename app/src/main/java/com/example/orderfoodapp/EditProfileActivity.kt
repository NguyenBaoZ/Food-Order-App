package com.example.orderfoodapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.lang.Exception

class EditProfileActivity : AppCompatActivity() {

    private var key = ""
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        key = intent.getStringExtra("key").toString()
        val customerEmail = Firebase.auth.currentUser?.email.toString()

        //load avatar from fire storage
        val imgName = customerEmail.replace(".", "_")
        val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imgName.jpg")
        try {
            val localFile = File.createTempFile("tempfile", ".jpg")
            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                avatar_image.setImageBitmap(bitmap)
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

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

                uploadImage(edtEmail.text.toString())

                btnUpdate.visibility = View.GONE
                Toast.makeText(this, "Update successfully!", Toast.LENGTH_LONG).show()
            }
            catch (e: Exception) {
                Toast.makeText(this, "Update failed! Please try again!", Toast.LENGTH_LONG).show()
            }
        }

        camera_button.setOnClickListener() {
            selectImage()
        }
    }

    private fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            avatar_image.setImageURI(imageUri)
            btnUpdate.visibility = View.VISIBLE
        }
    }

    private fun uploadImage(email: String) {
        val imgName = email.replace(".", "_")
        val storageRef = FirebaseStorage.getInstance().getReference("avatar_image/$imgName.jpg")
        storageRef.putFile(imageUri)
    }
}