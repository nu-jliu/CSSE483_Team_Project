package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import edu.rosehulman.fangr.kitchenkit.ingredient.Ingredient
import edu.rosehulman.fangr.kitchenkit.ingredient.MyIngredientsFragment

class RecipeAdapter(private val context: Context,
                    private val category: String,
                    private val listener: RecipeBrowserFragment.OnButtonPressedListener
) :
    RecyclerView.Adapter<RecipeViewHolder>() {

    private val recipes = ArrayList<Recipe>()
    private var recipeReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.RECIPE_COLLECTION)

    private var listenerRegistration: ListenerRegistration? = null

    init {
        this.showAll()
    }

    private fun addListenerAll() {
        listenerRegistration = this.recipeReference.orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    Log.d(Constants.TAG, "Cat: " + recipe.category.toString() + " " + category)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (recipe.category.contains(category)) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                this.recipes.add(0, recipe)
                                this.notifyItemInserted(0)
                            }
                            DocumentChange.Type.REMOVED -> {
                                this.recipes.removeAt(position)
                                this.notifyItemRemoved(position)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                this.recipes[position] = recipe
                                this.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
    }

    private fun addListenerFiltered(filter: String) {
        listenerRegistration = this.recipeReference.orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (recipe.name.contains(filter) && recipe.category.contains(category)) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                this.recipes.add(0, recipe)
                                this.notifyItemInserted(0)
                            }
                            DocumentChange.Type.REMOVED -> {
                                this.recipes.removeAt(position)
                                this.notifyItemRemoved(position)
                            }
                            DocumentChange.Type.MODIFIED -> {
                                this.recipes[position] = recipe
                                this.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view =
            LayoutInflater.from(this.context).inflate(R.layout.recipe_card_view, parent, false)
        return RecipeViewHolder(view, this)
    }

    override fun getItemCount(): Int = this.recipes.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(this.recipes[position])
    }

    fun showAll() {
        listenerRegistration?.remove()
        recipes.clear()
        notifyDataSetChanged()
        addListenerAll()

    }

    fun showFiltered(filter: String) {
        listenerRegistration?.remove()
        recipes.clear()
        notifyDataSetChanged()
        addListenerFiltered(filter)
    }

    fun selectRecipeAt(adapterPosition: Int) {
        val recipeID = this.recipes[adapterPosition].id
        this.listener.onRecipeSelected(recipeID)
    }
}