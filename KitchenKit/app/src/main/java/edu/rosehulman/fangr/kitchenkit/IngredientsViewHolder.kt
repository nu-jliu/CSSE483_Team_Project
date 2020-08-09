package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_ingredient_view.view.*
import kotlinx.android.synthetic.main.ingredient_card_view.view.*

class IngredientsViewHolder(
    itemView: View,
    adapter: IngredientsAdapter,
    private val context: Context
) :
    RecyclerView.ViewHolder(itemView) {

    private val nameTextView = itemView.ingredient_name as TextView
    private val amountTextView = itemView.amount_num as TextView
    private val isFrozenView = itemView.snow_icon as ImageButton
    private val boughtTextView = itemView.bought_time as TextView
    private val imageView = itemView.ingredient_image

    init {
        this.itemView.setOnClickListener {
            adapter.selectIngredientAt(this.adapterPosition)
        }
    }

    fun bind(ingredient: Ingredient) {
        this.nameTextView.text = ingredient.name
        this.amountTextView.text =
            this.context.getString(R.string.amount_display, ingredient.amount)
        if(!ingredient.isFrozen)
            this.isFrozenView.setImageResource(android.R.color.white)

        if (ingredient.bought == null) {
            this.boughtTextView.text = this.context.getString(R.string.zero_day_display)
            return
        }
        if (Utils.existsIngredient(ingredient.name)) {
            Picasso.get().load(Utils.getIngUrlFromName(ingredient.name)).into(imageView)
        }

        val time = ingredient.bought?.toDate()?.time
        val currentTime = Timestamp.now().toDate().time

        Log.d(Constants.TAG, "bought: $time")
        Log.d(Constants.TAG, "current: $currentTime")

        val difference = currentTime - time!!
        val days = (difference / (1000 * 60 * 60 * 24)).toInt()
        Log.d(Constants.TAG, "days: $days")
        if (days == 0)
            this.boughtTextView.text = this.context.getString(R.string.zero_day_display)
        else
            this.boughtTextView.text = this.context.resources.getQuantityString(
                R.plurals.day_display,
                days,
                days
            )
    }
}