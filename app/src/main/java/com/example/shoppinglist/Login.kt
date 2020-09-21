package com.example.shoppinglist

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputPass: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var gotoCreateAc: TextView
    // Firebase
    private lateinit var myAuth: FirebaseAuth
    private lateinit var myAuthStateListener: FirebaseAuth.AuthStateListener
    private var myUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        bindViews()
        setUpFirebase()
        clickListeners()
    }

    private fun setUpFirebase() {
        //myAuth = FirebaseAuth.getInstance()                                   // Java Style
        myAuth = Firebase.auth                                                  // Kotlin Style
        myAuthStateListener = FirebaseAuth.AuthStateListener{ firebaseAuth ->
            myUser = firebaseAuth.currentUser
        }
    }

    private fun clickListeners() {
        loginButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                var email = inputEmail.editText?.text.toString().trim()
                var password = inputPass.editText?.text.toString().trim()

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    inputEmail.error = null
                    inputPass.error = null

                    var myAlertDialog =
                        AlertDialog.Builder(this@Login).setCancelable(false).setView(
                            R.layout.progress_bar
                        ).create()
                    myAlertDialog.show()

                    myAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this@Login) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@Login,
                                    "Successfully Logged In",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(Intent(this@Login, MainActivity::class.java))
                                finish()
                            } else
                                Toast.makeText(
                                    this@Login,
                                    "Something is wrong",
                                    Toast.LENGTH_SHORT
                                ).show()

                            myAlertDialog.dismiss()
                        }
                } else {
                    if(email.isNotEmpty())
                        inputEmail.error = null
                    else
                        inputEmail.error = "Required"

                    if (password.isNotEmpty())
                        inputPass.error = null
                    else
                        inputPass.error = "Required"
                }
            }
        })

        gotoCreateAc.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                startActivity(Intent(this@Login, CreateAccount::class.java))
                finish()
            }
        })
    }

    private fun bindViews() {
        inputEmail = findViewById(R.id.emailID)
        inputPass = findViewById(R.id.passID)
        loginButton = findViewById(R.id.loginBtnID)
        gotoCreateAc = findViewById(R.id.createGoID)
    }

    override fun onStart() {
        super.onStart()
        myAuth.addAuthStateListener(myAuthStateListener)
    }
}