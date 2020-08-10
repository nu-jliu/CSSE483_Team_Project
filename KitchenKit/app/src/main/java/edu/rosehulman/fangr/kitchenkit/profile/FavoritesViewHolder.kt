package edu.rosehulman.fangr.kitchenkit.profile

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.rosehulman.fangr.kitchenkit.profile.FavoritesAdapter
import kotlinx.android.synthetic.main.favorites_row_view.view.*

class FavoritesViewHolder(itemView: View, adapter: FavoritesAdapter) :
    RecyclerView.ViewHolder(itemView) {

    private val favoriteTextView = itemView.favorite_text_view as TextView
    private val numberFavoriteTextView = itemView.favorite_number as TextView

    init {
        itemView.setOnClickListener {
            adapter.showAddDialog(this.adapterPosition)
        }
    }

    fun bind(favorite: String) {
        this.favoriteTextView.text = favorite
        this.numberFavoriteTextView.text = "${this.adapterPosition + 1}. "
    }

}