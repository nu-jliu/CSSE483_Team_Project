package edu.rosehulman.fangr.kitchenkit

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.rosehulman.rosefire.Rosefire
import kotlinx.android.synthetic.main.custom_ingredient_alert_view.view.*
import kotlinx.android.synthetic.main.custom_ingredient_alert_view.view.name_edit_text
import kotlinx.android.synthetic.main.search_alert_view.view.*

class MainActivity : AppCompatActivity(),
    SplashFragment.OnSignInButtonPressedListener,
    RecipeBrowserFragment.OnButtonPressedListener,
    ProfileFragment.OnButtonsPressedListener,
    MyIngredientsFragment.OnButtonPressedListener,
    AddIngredientFragment.OnAddButtonPressedListener,
    EditProfileFragment.OnButtonsPressedListener,
    EditIngredientFragment.OnIngredientSaveButtonPressedListener {

    private val auth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    private val RC_SIGN_IN = 1
    private val RC_ROSE_SIGN_IN = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_main)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == this.RC_ROSE_SIGN_IN) {
            val result = Rosefire.getSignInResultFromIntent(data)
            if (result.isSuccessful) {
                this.auth.signInWithCustomToken(result.token)
                Log.d(Constants.TAG, "Username: ${result.username}")
                Log.d(Constants.TAG, "Name: ${result.name}")
                Log.d(Constants.TAG, "E-Mail: ${result.email}")
                Log.d(Constants.TAG, "Group: ${result.group}")
            } else
                Log.d(Constants.TAG, "Rosefire Failed")
        }
    }

    private fun initializeListeners() {
        this.authStateListener = FirebaseAuth.AuthStateListener { auth: FirebaseAuth ->
            val user = auth.currentUser
            if (user != null) {
                Log.d(Constants.TAG, "UID: ${user.uid}")
                Log.d(Constants.TAG, "Name: ${user.displayName}")
                Log.d(Constants.TAG, "E-Mail: ${user.email}")
                Log.d(Constants.TAG, "Phone: ${user.phoneNumber}")
                Log.d(Constants.TAG, "Photo: ${user.photoUrl}")
                this.switchTo(RecipeBrowserFragment())
            } else
                this.switchTo(SplashFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menuInflater.inflate(R.menu.menu_main, menu)
        return true
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
                .setLogo(R.drawable.ic_logo)
                .build()

        this.startActivityForResult(loginIntent, this.RC_SIGN_IN)
    }

    private fun switchTo(fragment: Fragment) {
        val ft = this.supportFragmentManager.beginTransaction()
        val currentFragment = this.supportFragmentManager.findFragmentById(R.id.fragment_container)
        ft.replace(R.id.fragment_container, fragment)
        if (fragment !is RecipeBrowserFragment && currentFragment !is AddIngredientFragment && fragment !is SplashFragment)
            ft.addToBackStack(null)
        else
            this.supportFragmentManager.popBackStackImmediate()
        ft.commit()
    }

    override fun onSignInButtonPressed() {
        this.launchLoginUI()
    }

    override fun onRoseSignInButtonPressed() {
        val roseFireSignInIntent =
            Rosefire.getSignInIntent(this, this.getString(R.string.token_rosefire_log_in))
        this.startActivityForResult(roseFireSignInIntent, this.RC_ROSE_SIGN_IN)
    }

    override fun onProfileButtonPressed() {
        this.auth.currentUser?.uid
            ?.let { ProfileFragment.newInstance(it) }
            ?.let { this.switchTo(it) }
    }

    override fun onMyIngredientsButtonPressed() {
        this.auth.currentUser?.uid
            ?.let { MyIngredientsFragment.newInstance(it) }
            ?.let { this.switchTo(it) }
    }

    override fun onLogoutPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.logout_alert)
        builder.setNegativeButton(android.R.string.no, null)
        builder.setPositiveButton(android.R.string.yes) { _, _ ->
            this.auth.signOut()
        }
        builder.create().show()
    }

    override fun onProfileBackPressed() {
        this.switchTo(RecipeBrowserFragment())
    }

    override fun onEditButtonPressed() {
        this.auth.currentUser?.uid
            ?.let { EditProfileFragment.newInstance(it) }
            ?.let { this.switchTo(it) }
    }

    override fun onMyIngredientsFragmentBackButtonPressed() {
        this.onProfileBackPressed()
    }

    override fun onAddIngredientFragmentBackButtonPressed() {
        this.onMyIngredientsButtonPressed()
    }

    override fun onAddFABPressed(ingredientList: ArrayList<String>) {
        this.auth.currentUser?.uid
            ?.let { AddIngredientFragment.newInstance(it, ingredientList) }
            ?.let { this.switchTo(it) }
    }

    override fun onAddButtonPressed() {
        this.onMyIngredientsButtonPressed()
    }

    override fun onButtonsPressed() {
        this.auth.currentUser?.uid
            ?.let { ProfileFragment.newInstance(it) }
            ?.let { this.switchTo(it) }
    }

    override fun onCustomizeIngredientButtonPressed() {
        val builder = AlertDialog.Builder(this)
        val view =
            LayoutInflater.from(this).inflate(R.layout.custom_ingredient_alert_view, null, false)
        builder.setView(view)

        val ingRef = FirebaseFirestore.getInstance().collection("storedIngredient")
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
            val name = view.name_edit_text.text.toString()
            val url = view.url_edit_text.text.toString()
            val expire1 = view.expire_edit_text_1.text.toString()
            val expire2 = view.expire_edit_text_2.text.toString()
            val expireFrozen1 = view.expire_frozen_edit_text_1.text.toString()
            val expireFrozen2 = view.expire_frozen_edit_text_2.text.toString()
            val canFroze = view.can_froze_button.isChecked
            Log.d(
                Constants.TAG,
                "customize ingredient: $name$url$expire1$expire2$expireFrozen1$expireFrozen2$canFroze"
            )

            val ing = StoredIngredient(
                name,
                url,
                expire1,
                expire2,
                expireFrozen1,
                expireFrozen2,
                canFroze
            )
            ingRef.add(ing)
        })
        builder.create().show()
    }

    override fun onIngredientSearchButtonPressed(adapter: IngredientsAdapter?) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.search_alert_view, null, false)
        builder.setView(view)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { _, _ ->
            val filter = view.filter_edit_text.text.toString()
            adapter?.showFiltered(filter)
        })
        builder.setNeutralButton(R.string.clear_search_filter) { _, _ ->
            adapter?.showAll()
            view.filter_edit_text.setText("")
        }

        builder.create().show()
    }

    override fun onIngredientSelected(ingredientID: String) {
        this.auth.currentUser?.uid
            ?.let { EditIngredientFragment.newInstance(it, ingredientID) }
            ?.let { this.switchTo(it) }
    }

    override fun onSaveButtonPressed() {
        this.onMyIngredientsButtonPressed()
    }
}