package edu.rosehulman.fangr.kitchenkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.rosehulman.fangr.kitchenkit.ui.main.RecommenedRecipeViewModelFragment

class RecommenedRecipeViewModel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommened_recipe_page)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RecommenedRecipeViewModelFragment.newInstance())
                .commitNow()
        }
    }
}