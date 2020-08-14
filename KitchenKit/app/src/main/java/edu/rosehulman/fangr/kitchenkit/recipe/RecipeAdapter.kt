package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import edu.rosehulman.fangr.kitchenkit.ingredient.Ingredient

class RecipeAdapter(
    private val context: Context,
    val category: String,
    private val categories: ArrayList<String>,
    private val listener: RecipeBrowserFragment.OnButtonPressedListener,
    private val uid: String? = null
) :
    RecyclerView.Adapter<RecipeViewHolder>() {
    private var showRecommended = false
    private val recipes = ArrayList<Recipe>()
    private var recipeReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.RECIPE_COLLECTION)
    private val ingredientReference =
        uid?.let {
            FirebaseFirestore
                .getInstance()
                .collection(Constants.USER_COLLECTION)
                .document(it)
                .collection(Constants.INGREDIENT_COLLECTION)
        }
    private val myIngredients: ArrayList<String> = ArrayList()
    private var listenerRegistration: ListenerRegistration? = null

    init {
        this.showAll()
        getMyIngredients()
    }

    private fun getMyIngredients() {
        ingredientReference?.addSnapshotListener { snapshot: QuerySnapshot?, e ->
            if (e != null) {
                Log.e(Constants.TAG, "EXCEPTION $e")
                return@addSnapshotListener
            }
            for (docChange in snapshot?.documentChanges!!) {
                val temp = Ingredient.fromSnapshot(docChange.document)
                myIngredients.add(temp.name)
            }
            Log.d(Constants.TAG, "found ingredients: $myIngredients")
        }
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
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
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

    private fun addListenerRecommended() {
        this.listenerRegistration = this.recipeReference
            .orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION $exception")
                    return@addSnapshotListener
                }
                var x = 0
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (hasEnoughIngredients(recipe, myIngredients, recipe.recommendPercentage)) {
                        x++
                        this.processChange(docChange.type, recipe, position)
                    }
                }
                if (x == 0) {
                    Toast.makeText(
                        context,
                        "You have too few ingredients!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun hasEnoughIngredients(recipe: Recipe, list: ArrayList<String>, percentage: Float): Boolean {
        val ingArray = recipe.ingArray
        val m = ingArray.size
        var n = 0
        for (i in list.indices) {
            if (ingArray.contains(list[i])) {
                n++
            }
        }
        if (n/m >= percentage) {
            return true
        }
        return false
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

    private fun showRecommended() {
        this.listenerRegistration?.remove()
        this.recipes.clear()
        this.notifyDataSetChanged()
        this.addListenerRecommended()
    }

    fun selectRecipeAt(adapterPosition: Int) {
        val recipeID = this.recipes[adapterPosition].id
        this.listener.onRecipeSelected(recipeID)
    }

    fun toggleRecommendedViewOption(): Boolean {
        this.showRecommended = !this.showRecommended
        if (this.showRecommended)
            this.showRecommended()
        else
            this.showAll()
        return this.showRecommended
    }
}