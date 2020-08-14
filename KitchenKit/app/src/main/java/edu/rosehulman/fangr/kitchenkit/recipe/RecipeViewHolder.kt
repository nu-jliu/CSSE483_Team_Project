package edu.rosehulman.fangr.kitchenkit.recipe

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.ingredient.IngredientsAdapter
import kotlinx.android.synthetic.main.recipe_card_view.view.*

class RecipeViewHolder(itemView: View, adapter: RecipeAdapter) : RecyclerView.ViewHolder(itemView) {

    private val foodNameTextView = itemView.food_name as TextView
    private val numIngredientsTextView = itemView.num_ingredients as TextView
    private val caloriesTextView = itemView.num_calories as TextView
    private val timeTextView = itemView.time_needed as TextView
    private val foodImageView = itemView.food_image as ImageView

    init {
        this.itemView.setOnClickListener {
            adapter.selectRecipeAt(adapterPosition)
        }
    }

    fun bind(recipe: Recipe) {
        this.foodNameTextView.text = recipe.name
        this.numIngredientsTextView.text = recipe.amountIng.toString()
        this.caloriesTextView.text = recipe.amountCal.toString()
        this.timeTextView.text = "${recipe.amountTime / 60}h${recipe.amountTime % 60}m"
        Picasso.get().load(recipe.url).into(this.foodImageView)
    }

}