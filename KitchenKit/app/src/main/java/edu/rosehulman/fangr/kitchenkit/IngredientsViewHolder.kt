package edu.rosehulman.fangr.kitchenkit

import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import kotlinx.android.synthetic.main.ingredient_card_view.view.*

class IngredientsViewHolder(itemView: View, adapter: IngredientsAdapter) : RecyclerView.ViewHolder(itemView) {

    private val nameTextView = itemView.ingredient_name as TextView
    private val amountTextView = itemView.amount_num as TextView
    private val isFrozenView = itemView.snow_icon as ImageButton
    private val boughtTextView = itemView.bought_time as TextView

    fun bind(ingredient: Ingredient) {
        this.nameTextView.text = ingredient.name
        this.amountTextView.text = ingredient.amount.toString() + " kg"
        this.isFrozenView.isVisible = ingredient.isFrozen
        val time = ingredient.bought?.toDate()?.time
        val currentTime = Timestamp.now().toDate().time
        val difference = currentTime - time!!
        val days = (difference / (1000 * 60 * 60 * 24))
        this.boughtTextView.text = "$days days ago"
    }

}