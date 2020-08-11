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
import java.lang.RuntimeException

class RecipeAdapter(
    private val context: Context,
    val category: String,
    private val categories: ArrayList<String>,
    private val listener: RecipeBrowserFragment.OnButtonPressedListener
) :
    RecyclerView.Adapter<RecipeViewHolder>() {

    private var showFavorite = false
    private val recipes = ArrayList<Recipe>()
    private var recipeReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.RECIPE_COLLECTION)

    private var listenerRegistration: ListenerRegistration? = null

    init {
        this.showAll()
    }

    private fun processChange(type: DocumentChange.Type, recipe: Recipe, position: Int) {
        when (type) {
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

    private fun addListenerAll() {
        this.listenerRegistration = this.recipeReference
            .orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .whereArrayContains(Constants.KEY_CATEGORY, this.category)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    Log.d(Constants.TAG, "Cat: ${recipe.category} ${this.category}")
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
//                    if (recipe.category.contains(Constants.VALUE_ALL))
                    this.processChange(docChange.type, recipe, position)
                }
            }
    }

    private fun addListenerFiltered(filter: String) {
        this.listenerRegistration = this.recipeReference
            .orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .whereArrayContains(Constants.KEY_CATEGORY, this.category)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (recipe.name.contains(filter))
                        this.processChange(docChange.type, recipe, position)
                }
            }
    }

    private fun addListenerFavorite() {
        Log.d(Constants.TAG, "size: ${this.categories.size}, ${this.categories}")
        if (this.categories.isEmpty())
            return
        this.listenerRegistration = this.recipeReference
            .orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .whereArrayContainsAny(Constants.KEY_CATEGORY, this.categories)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION $exception")
                    return@addSnapshotListener
                }
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    this.processChange(docChange.type, recipe, position)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater
            .from(this.context)
            .inflate(R.layout.recipe_card_view, parent, false)
        return RecipeViewHolder(view, this)
    }

    override fun getItemCount(): Int = this.recipes.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(this.recipes[position])
    }

    fun showAll() {
        this.listenerRegistration?.remove()
        this.recipes.clear()
        this.notifyDataSetChanged()
        this.addListenerAll()

    }

    fun showFiltered(filter: String) {
        this.listenerRegistration?.remove()
        this.recipes.clear()
        this.notifyDataSetChanged()
        this.addListenerFiltered(filter)
    }

    private fun showFavoriteCategories() {
        this.listenerRegistration?.remove()
        this.recipes.clear()
        this.notifyDataSetChanged()
        this.addListenerFavorite()
    }

    fun selectRecipeAt(adapterPosition: Int) {
        val recipeID = this.recipes[adapterPosition].id
        this.listener.onRecipeSelected(recipeID)
    }

    fun toggleFavoriteViewOption(): Boolean {
        this.showFavorite = !this.showFavorite
        if (this.showFavorite)
            this.showFavoriteCategories()
        else
            this.showAll()
        return this.showFavorite
    }
}