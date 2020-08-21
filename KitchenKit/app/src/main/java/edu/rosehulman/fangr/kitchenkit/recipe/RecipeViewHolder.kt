package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.recipe_card_view.view.*

class RecipeViewHolder(itemView: View, adapter: RecipeAdapter, private val context: Context) :
    RecyclerView.ViewHolder(itemView) {

    private val foodNameTextView = itemView.food_name as TextView
    private val numIngredientsTextView = itemView.num_ingredients as TextView
    private val caloriesTextView = itemView.num_calories as TextView
    private val timeTextView = itemView.time_needed as TextView
    private val foodImageView = itemView.food_image as ImageView
    private val likeButtonImageButton = itemView.button_like as ImageButton

    init {
        this.itemView.setOnClickListener {
            adapter.selectRecipeAt(this.adapterPosition)
        }

        this.itemView.button_like.setOnClickListener {
            adapter.likeRecipeAt(this.adapterPosition)
        }
    }

    fun bind(recipe: Recipe, isLiked: Boolean) {
        this.foodNameTextView.text = recipe.name
        this.numIngredientsTextView.text = recipe.amountIng.toString()
        this.caloriesTextView.text = recipe.amountCal.toString()
        this.timeTextView.text = this.context.getString(
            R.string.time_format,
            recipe.amountTime / 60,
            recipe.amountTime % 60
        )
        Picasso.get().load(recipe.url).into(this.foodImageView)

        if (isLiked)
            this.likeButtonImageButton.setImageResource(android.R.drawable.btn_star_big_on)
        else
            this.likeButtonImageButton.setImageResource(android.R.drawable.btn_star_big_off)
    }

}