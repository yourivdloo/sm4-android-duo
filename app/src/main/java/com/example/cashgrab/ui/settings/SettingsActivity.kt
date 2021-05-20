package com.example.cashgrab.ui.settings

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.cashgrab.R
import com.example.cashgrab.databinding.ActivitySettingsBinding
import com.example.cashgrab.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.linkSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.linkGameRules.setOnClickListener {
            var myDialog: Dialog
            myDialog = Dialog(this)
            myDialog.setContentView(R.layout.popup_rules);
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val backButton = myDialog.findViewById<ImageButton>(R.id.buttonBack9)

            backButton.setOnClickListener {
                myDialog.dismiss()
            }
        }

        binding.linkQuitGame.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you sure!")
            builder.setMessage("Do you want to close the app?")
            builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                finishAffinity()
            }
            builder.setNegativeButton("No") { _: DialogInterface, _: Int -> }
            builder.show()
        }

        binding.linkAppSettings.setOnClickListener {
            notImplementedToast()
        }

        binding.linkAccountSettings.setOnClickListener {
            notImplementedToast()
        }

        binding.linkPrivacySettings.setOnClickListener {
            notImplementedToast()
        }
    }

    private fun notImplementedToast(){
        Toast.makeText(
            this,
            "This feature has not been implemented yet.",
            Toast.LENGTH_SHORT
        ).show();
    }
}