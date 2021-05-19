package com.example.cashgrab.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.cashgrab.databinding.ActivityRegisterBinding
import com.example.cashgrab.models.User
import com.example.cashgrab.ui.MainActivity
import com.example.cashgrab.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val db = Firebase.firestore

        binding.linkLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.textUsername.addTextChangedListener { text ->
            if(text?.isNotEmpty()!! && text?.length!! > 16){
                binding.textUsername.setText(text.substring(0,16))
                binding.textUsername.setSelection(binding.textUsername.text.toString().length)
                Toast.makeText(
                    this, "Your username can be a maximum of 16 characters.",
                    Toast.LENGTH_SHORT
                ).show();
            }
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.textEmail.text.toString()
            val username = binding.textUsername.text.toString()
            val password = binding.textPassword.text.toString()
            val cPassword = binding.textConfirmPassword.text.toString()

            db.collection("users")
                .get()
                .addOnSuccessListener { documents ->
                    val users = arrayListOf<String>()

                    for (document in documents) {
                        users.add(document.data.get("user_name").toString())
                    }
                    var exists = false

                    for (user in users) {
                        if (username == user) {
                            exists = true
                        }
                    }

                    if (!exists && email.isNotEmpty() && username.isNotEmpty() && username.length <= 16 && password.isNotEmpty() && password == cPassword && email.contains(
                            "@"
                        ) && email.contains(".")
                    ) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    val currentUser = auth.currentUser
                                    if (currentUser != null) {
                                        val uid = currentUser.uid

                                        val cal = Calendar.getInstance()
                                        cal.add(Calendar.DAY_OF_YEAR, -1)
                                        var date = cal.time

                                        val user = User(
                                            uid,
                                            "",
                                            date,
                                            date,
                                            date,
                                            date,
                                            15,
                                            date,
                                            false,
                                            false,
                                            0,
                                            10000,
                                            0,
                                            0,
                                            0,
                                            username
                                        )
                                        db.collection("users").add(user)
                                            .addOnCompleteListener(this) {
                                                if (it.isSuccessful) {
                                                    val intent =
                                                        Intent(this, MainActivity::class.java)
                                                    startActivity(intent)
                                                    Log.d("SUCCESS", "createUserWithEmail:success")
                                                } else {
                                                    failureToast()
                                                    currentUser.delete()
                                                    Log.w("FAILURE", "createUserWithEmail:failure");
                                                }
                                            }
                                    }
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("FAILURE", "createUserWithEmail:failure");
                                    failureToast()
                                }
                            }
                    } else {
                        failureToast()
                    }
                }
        }
    }

    private fun failureToast() {
        Toast.makeText(
            this, "Registration failed.",
            Toast.LENGTH_SHORT
        ).show();
    }
}