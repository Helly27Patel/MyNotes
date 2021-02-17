package com.example.mynotesstudent.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mynotesstudent.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginRegisterActivity : AppCompatActivity() {

    val REQUEST_CODE = 1001
    val TAG = "LoginRegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)

        if (FirebaseAuth.getInstance().currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or (Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            Log.d(TAG, "User Already Authenticated")
        }
    }

    fun userLoginRegister(view: View) {

        val provider: ArrayList<AuthUI.IdpConfig> = ArrayList()
        provider.add(AuthUI.IdpConfig.EmailBuilder().build()) // i can add custom email and password
        provider.add(AuthUI.IdpConfig.GoogleBuilder().build()) // i will be allowed to enter application with google account
        provider.add(AuthUI.IdpConfig.PhoneBuilder().build()) // i will be allowed to enter application with google account

        val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provider)
                .setIsSmartLockEnabled(false)
                .setTosAndPrivacyPolicyUrls("https://google.com", "https://google.com")
                .build()

        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE == requestCode) {
            if (resultCode == RESULT_OK) {

                val user = FirebaseAuth.getInstance().currentUser
                Log.d(TAG, "${user!!.email}")

                if (user.metadata!!.creationTimestamp == user.metadata!!.lastSignInTimestamp) {
                    Toast.makeText(this, "Welcome New User", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Welcome Again", Toast.LENGTH_LONG).show()
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or (Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Error Occurs", Toast.LENGTH_LONG).show()
                //signin failed
                val response = IdpResponse.fromResultIntent(data)
                if (response == null) {
                    Log.d(TAG, "onActivityResult: The user has cancelled the sign in request")
                } else {
                    Log.d(TAG, "onActivityResult:${response.error}")
                }
            }
        }
    }
}