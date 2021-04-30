package com.example.cashgrab.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.cashgrab.ui.MainActivity
import com.example.cashgrab.ui.register.RegisterActivity
import com.example.cashgrab.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.buttonLogin.setOnClickListener{
            val email = binding.textEmail.text.toString()
            val password = binding.textPassword.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("SUCCESS", "signInWithEmail:success")
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("FAILURE", "signInWithEmail:failure")
                                failureToast()
                            }
                        }
            } else {
                failureToast()
            }
        }

        binding.linkRegister.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun failureToast(){
        Toast.makeText(
                this, "Authentication failed.",
                Toast.LENGTH_SHORT
        ).show();
    }
}