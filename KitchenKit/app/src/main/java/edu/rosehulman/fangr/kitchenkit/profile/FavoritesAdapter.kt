package edu.rosehulman.fangr.kitchenkit.profile

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.category_add.view.*
import java.util.*
import kotlin.collections.ArrayList

@ExperimentalStdlibApi
class FavoritesAdapter(private val context: Context, private val uid: String) :
    RecyclerView.Adapter<FavoritesViewHolder>() {

    private val favorites = ArrayList<String>()
    private val categoryIDs = ArrayList<String>()
    private val favoritesReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.USER_COLLECTION)
        .document(this.uid)
        .collection(Constants.FAVORITES_COLLECTION)

    init {
        this.favoritesReference.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
            if (exception != null) {
                Log.e(Constants.TAG, "EXCEPTION: $exception")
                return@addSnapshotListener
            }
            for (docChange in snapshot?.documentChanges!!) {
                val category = docChange.document.data[Constants.KEY_CATEGORY].toString()
                val id = docChange.document.id
                val position = this.categoryIDs.indexOfFirst { it == id }
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        this.favorites.add(this.favorites.size, category)
                        this.categoryIDs.add(id)
                        this.notifyItemInserted(this.favorites.size)
                    }
                    DocumentChange.Type.REMOVED -> {
                        this.favorites.removeAt(position)
                        this.notifyItemRemoved(position)
                        for (index in position until this.favorites.size)
                            this.notifyItemChanged(index)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        this.favorites[position] = category
                        this.notifyItemChanged(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater
            .from(this.context)
            .inflate(R.layout.favorites_row_view, parent, false)
        return FavoritesViewHolder(view, this)
    }

    override fun getItemCount(): Int = this.favorites.size

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        holder.bind(this.favorites[position])
    }

    @ExperimentalStdlibApi
    fun showAddDialog(position: Int = -1) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle(
            if (position == -1)
                "Add a Category"
            else
                "Edit a Category"
        )

        val view = LayoutInflater.from(this.context).inflate(R.layout.category_add, null, false)
        val spinner = view.category_spinner
        val categories = arrayListOf(
            Constants.VALUE_DINNER,
            Constants.VALUE_VEGAN,
            Constants.VALUE_SNACK
        )
        val adapter = ArrayAdapter(
            this.context,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        spinner.adapter = adapter
        if (position != -1) {
            val category = this.favorites[position].toLowerCase()
            val index = categories.indexOfFirst { it == category }
            spinner.setSelection(index)
        }
        builder.setView(view)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            if (position == -1) {
                val selectedCategory = spinner.selectedItem.toString().capitalize(Locale.ROOT)
                if (this.favorites.contains(selectedCategory)) {
                    Toast.makeText(
                        this.context,
                        "The selected category has already been added to the favorite list",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }
                this.favoritesReference.add(
                    mapOf(
                        Pair(
                            Constants.KEY_CATEGORY,
                            spinner.selectedItem.toString().capitalize(Locale.ROOT)
                        )
                    )
                )
            } else
                this.favoritesReference
                    .document(this.categoryIDs[position])
                    .set(
                        mapOf(
                            Pair(
                                Constants.KEY_CATEGORY,
                                spinner.selectedItem.toString().capitalize(Locale.ROOT)
                            )
                        )
                    )
        }
        builder.setNegativeButton(android.R.string.cancel, null)

        if (position != -1)
            builder.setNeutralButton(R.string.action_delete) { _, _ ->
                this.favoritesReference.document(this.categoryIDs[position]).delete()
            }

        builder.create().show()
    }
}