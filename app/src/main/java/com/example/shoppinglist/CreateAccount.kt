package com.example.shoppinglist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CreateAccount : AppCompatActivity() {

    private lateinit var createAccountButton: Button
    private lateinit var gotoLoginButton: TextView
    private lateinit var inputEmail: TextInputLayout
    private lateinit var inputPassword: TextInputLayout
    //Firebase
    private lateinit var myAuth: FirebaseAuth
    private lateinit var myAuthStateListener: FirebaseAuth.AuthStateListener
    private var myUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        bindView()
        setUpFirebase()
        clickListeners()
    }

    private fun setUpFirebase() {
        myAuth = Firebase.auth
        myAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            myUser = firebaseAuth.currentUser
        }
    }

    private fun clickListeners() {
        createAccountButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {

                var email = inputEmail.editText?.text.toString().trim()
                var password = inputPassword.editText?.text.toString().trim()

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    inputEmail.error = null
                    inputPassword.error = null

                    var myAlertDialog = AlertDialog.Builder(this@CreateAccount).setCancelable(false)
                        .setView(R.layout.progress_bar)
                        .create()
                    myAlertDialog.show()

                    myAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@CreateAccount, "Account created", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@CreateAccount, MainActivity::class.java))
                            finish()
                        }
                        else
                            Toast.makeText(this@CreateAccount, "Oops..! Something is wrong",Toast.LENGTH_SHORT).show()

                        myAlertDialog.dismiss()
                    }
                }
                else{
                    // setError in TextInputLayout
                    if(email.isNotEmpty())
                        inputEmail.error = null
                    else
                        inputEmail.error = "Required"

                    if (password.isNotEmpty())
                        inputPassword.error = null
                    else
                        inputPassword.error = "Required"
                }
            }
        })

        gotoLoginButton.setOnClickListener (object : View.OnClickListener{
            override fun onClick(v: View?) {
                startActivity(Intent(this@CreateAccount, Login::class.java))
                finish()
            }
        })
    }

    private fun bindView() {
        createAccountButton = findViewById(R.id.createAcButtonID)
        gotoLoginButton = findViewById(R.id.goLoginID)
        inputEmail = findViewById(R.id.inputEmailCAID)
        inputPassword = findViewById(R.id.inputPassCAID)
    }

    override fun onStart() {
        super.onStart()
        myAuth.addAuthStateListener(myAuthStateListener)
    }
}