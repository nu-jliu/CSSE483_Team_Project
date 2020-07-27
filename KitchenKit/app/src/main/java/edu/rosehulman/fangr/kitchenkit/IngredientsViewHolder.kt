package edu.rosehulman.fangr.kitchenkit

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.ingredient_card_view.view.*

class IngredientsViewHolder(itemView: View, adapter: IngredientsAdapter) : RecyclerView.ViewHolder(itemView) {

    private val nameTextView = itemView.ingredient_name as TextView
    private val amountTextView = itemView.amount_num as TextView
    private val isFrozenView = itemView.snow_icon as ImageButton

    fun bind(ingredient: Ingredient) {
        this.nameTextView.text = ingredient.name
        this.amountTextView.text = ingredient.amount.toString() + " kg"
        this.isFrozenView.isVisible = ingredient.isFrozen
    }

}