package com.example.shoppinglist

import OnBoard.OnBoarding
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SplashScreen: AppCompatActivity() {

    private lateinit var myAuth: FirebaseAuth
    private lateinit var myAuthStateListener: FirebaseAuth.AuthStateListener
    private var myUser: FirebaseUser? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(500)

        setUpFirebase()
        setUpSharedPref()

        if (myUser != null)
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
        else {
            if (sharedPreferences.getBoolean("doneCheck", false))
                startActivity(Intent(this@SplashScreen, Login::class.java))
            else
                startActivity(Intent(this@SplashScreen, OnBoarding::class.java))
        }

        finish()
    }

    private fun setUpSharedPref() {
        sharedPreferences = getSharedPreferences("OnBoardingCheck", MODE_PRIVATE)
    }

    private fun setUpFirebase() {
        myAuth = Firebase.auth
        myAuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth: FirebaseAuth ->
            //myUser = firebaseAuth.currentUser
        }
        myUser = myAuth.currentUser
    }

    override fun onStart() {
        super.onStart()
        myAuth.addAuthStateListener(myAuthStateListener)
    }
}