package edu.rosehulman.fangr.kitchenkit

import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.replace
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import java.lang.invoke.ConstantCallSite

class MainActivity : AppCompatActivity(), SplashFragment.OnSignInButtonPressedListener {

    private val auth = FirebaseAuth.getInstance()
    lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
        this.setSupportActionBar(findViewById(R.id.toolbar))
        this.initializeListeners()
    }

    override fun onStart() {
        super.onStart()
        this.auth.addAuthStateListener(this.authStateListener)
    }

    override fun onStop() {
        super.onStop()
        this.auth.removeAuthStateListener(this.authStateListener)
    }

    private fun initializeListeners() {
        this.authStateListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val user = auth.currentUser
            if (user != null) {
                Log.d(Constans.TAG, "UID: ${user.uid}")
                Log.d(Constans.TAG, "Name: ${user.displayName}")
                Log.d(Constans.TAG, "E-Mail: ${user.email}")
                Log.d(Constans.TAG, "Phone: ${user.phoneNumber}")
                Log.d(Constans.TAG, "Photo: ${user.photoUrl}")
                this.switchToMainFragment()
            } else
                this.switchToSplashFragment()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun switchToSplashFragment() {
        val ft = this.supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, SplashFragment())
        ft.commit()
    }

    private fun switchToMainFragment() {
        val ft = this.supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, BlankFragment())
        ft.commit()
    }

    override fun onSignInButtonPressed() {
        this.launchLoginUI()
    }

    private fun launchLoginUI() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val loginIntent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()

        this.startActivityForResult(loginIntent, this.RC_SIGN_IN)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                auth.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}