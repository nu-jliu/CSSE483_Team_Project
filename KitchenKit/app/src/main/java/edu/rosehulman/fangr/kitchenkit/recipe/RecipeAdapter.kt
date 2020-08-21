package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import edu.rosehulman.fangr.kitchenkit.ingredient.Ingredient

class RecipeAdapter(
    private val context: Context,
    val category: String,
    private val listener: RecipeBrowserFragment.OnButtonPressedListener,
    uid: String? = null
) :
    RecyclerView.Adapter<RecipeViewHolder>() {

    private var showRecommended = false
    private val recipes = ArrayList<Recipe>()
    private val myIngredients = ArrayList<String>()
    private val favoriteRecipes = HashMap<String, String>()
    private var listenerRegistration: ListenerRegistration? = null
    private var likeListenerRegistration: ListenerRegistration? = null

    private val recipeReference = FirebaseFirestore
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

    private val favoriteRecipesReference =
        uid?.let {
            FirebaseFirestore
                .getInstance()
                .collection(Constants.USER_COLLECTION)
                .document(it)
                .collection(Constants.FAVORITE_RECIPE_COLLECTION)
        }

    init {
        this.initFavoriteRecipesListListener()
        this.showAll()
        this.getMyIngredients()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater
            .from(this.context)
            .inflate(R.layout.recipe_card_view, parent, false)
        return RecipeViewHolder(view, this, this.context)
    }

    override fun getItemCount(): Int = this.recipes.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val isLiked =
            this.favoriteRecipes
                .keys
                .indexOfFirst { it == this.recipes[position].id } > -1
        Log.d(Constants.TAG, "Recipe $position is liked: $isLiked")
        holder.bind(this.recipes[position], isLiked)
    }

    private fun initFavoriteRecipesListListener() {
        Log.d(Constants.TAG, "Listener initialized")
        this.likeListenerRegistration = this.favoriteRecipesReference
            ?.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (documentChange in snapshot?.documentChanges!!) {
                    val recipeID = documentChange
                        .document
                        .get(Constants.KEY_RECIPE_ID)
                            as String
                    Log.d(Constants.TAG, "Recipe ID: $recipeID")
                    val id = documentChange.document.id
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> this.favoriteRecipes[recipeID] = id
                        DocumentChange.Type.REMOVED -> this.favoriteRecipes.remove(recipeID)
                        DocumentChange.Type.MODIFIED -> this.favoriteRecipes[recipeID] = id
                    }
                    this.notifyDataSetChanged()
                    Log.d(Constants.TAG, "favorite recipes: ${this.favoriteRecipes}")
                }
            }
    }

    private fun getMyIngredients() {
        this.ingredientReference?.addSnapshotListener { snapshot: QuerySnapshot?, e ->
            if (e != null) {
                Log.e(Constants.TAG, "EXCEPTION $e")
                return@addSnapshotListener
            }
            for (docChange in snapshot?.documentChanges!!) {
                val ingredient = Ingredient.fromSnapshot(docChange.document)
                this.myIngredients.add(ingredient.name)
            }
            Log.d(Constants.TAG, "found ingredients: ${this.myIngredients}")
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

    private fun addListenerLike() {
        this.listenerRegistration = this.recipeReference
            .orderBy(Recipe.NAME_KEY, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                for (documentChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(documentChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (this.favoriteRecipes.containsKey(recipe.id))
                        this.processChange(documentChange.type, recipe, position)
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
                var count = 0
                for (docChange in snapshot?.documentChanges!!) {
                    val recipe = Recipe.fromSnapshot(docChange.document)
                    val position = this.recipes.indexOfFirst { it.id == recipe.id }
                    if (hasEnoughIngredients(recipe, myIngredients, recipe.recommendPercentage)) {
                        count++
                        this.processChange(docChange.type, recipe, position)
                    }
                }
                if (count == 0) {
                    Toast.makeText(
                        this.context,
                        "You have too few ingredients!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun hasEnoughIngredients(
        recipe: Recipe,
        list: ArrayList<String>,
        percentage: Float
    ): Boolean {
        val ingArray = recipe.ingArray
        val size = ingArray.size
        var count = 0
        for (ingredient in list) {
            if (ingArray.contains(ingredient))
                count++
        }
        if (count / size >= percentage)
            return true
        return false
    }

    private fun resetListener() {
        this.listenerRegistration?.remove()
        this.recipes.clear()
    }

    fun showAll() {
        this.resetListener()
        if (this.category == Constants.VALUE_LIKE)
            this.addListenerLike()
        else
            this.addListenerAll()
        this.notifyDataSetChanged()
    }

    fun showFiltered(filter: String) {
        this.resetListener()
        this.addListenerFiltered(filter)
        this.notifyDataSetChanged()
    }

    private fun showRecommended() {
        this.resetListener()
        this.addListenerRecommended()
        this.notifyDataSetChanged()
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

    fun likeRecipeAt(position: Int) {
        val recipeID = this.recipes[position].id
        if (this.favoriteRecipes.containsKey(recipeID)) {
            val id = this.favoriteRecipes[recipeID]
            this.favoriteRecipesReference?.document(id!!)?.delete()
            return
        }
        this.favoriteRecipesReference?.add(mapOf(Pair(Constants.KEY_RECIPE_ID, recipeID)))
    }
}