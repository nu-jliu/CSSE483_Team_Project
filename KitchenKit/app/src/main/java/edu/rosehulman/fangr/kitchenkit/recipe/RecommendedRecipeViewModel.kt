package edu.rosehulman.fangr.kitchenkit.recipe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.rosehulman.fangr.kitchenkit.R
import edu.rosehulman.fangr.kitchenkit.ui.main.RecommenedRecipeViewModelFragment

class RecommendedRecipeViewModel : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recommended_recipe_page)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, RecommenedRecipeViewModelFragment.newInstance())
                .commitNow()
        }
    }
}