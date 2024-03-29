package edu.rosehulman.fangr.kitchenkit.ingredient

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R

@RequiresApi(Build.VERSION_CODES.O)
class IngredientsAdapter(
    private val uid: String,
    private val context: Context,
    private val recyclerView: RecyclerView,
    private val listener: MyIngredientsFragment.OnButtonPressedListener
) : RecyclerView.Adapter<IngredientsViewHolder>() {

    private val myIngredients = ArrayList<Ingredient>()

    private val ingredientsRef =
        FirebaseFirestore.getInstance()
            .collection(Constants.USER_COLLECTION)
            .document(this.uid)
            .collection(Constants.INGREDIENT_COLLECTION)

    private var listenerRegistration: ListenerRegistration? = null

    init {
        this.showAll()
    }

    private fun addListenerAll() {
        listenerRegistration =
            ingredientsRef.orderBy(Ingredient.BOUGHT_KEY, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        Log.e(Constants.TAG, "EXCEPTION: $exception")
                        return@addSnapshotListener
                    }
                    for (documentChange in snapshot!!.documentChanges) {
                        val ingredient = Ingredient.fromSnapshot(documentChange.document)
                        val position = this.myIngredients.indexOfFirst { ingredient.id == it.id }
                        this.processListener(documentChange.type, ingredient, position)
                    }
                }
    }

    private fun addListenerFiltered(filter: String) {
        listenerRegistration =
            ingredientsRef.orderBy(Ingredient.BOUGHT_KEY, Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null) {
                        Log.e(Constants.TAG, "EXCEPTION: $exception")
                        return@addSnapshotListener
                    }
                    for (documentChange in snapshot!!.documentChanges) {
                        val ingredient = Ingredient.fromSnapshot(documentChange.document)
                        val position = this.myIngredients.indexOfFirst { ingredient.id == it.id }
                        if (ingredient.name.contains(filter))
                            this.processListener(documentChange.type, ingredient, position)
                    }
                }
    }


    private fun processListener(type: DocumentChange.Type, ingredient: Ingredient, position: Int) {
        when (type) {
            DocumentChange.Type.ADDED -> {
                this.myIngredients.add(0, ingredient)
                this.notifyItemInserted(0)
                this.recyclerView.smoothScrollToPosition(0)
            }
            DocumentChange.Type.REMOVED -> {
                this.myIngredients.removeAt(position)
                this.notifyItemRemoved(position)
            }
            DocumentChange.Type.MODIFIED -> {
                this.myIngredients[position] = ingredient
                this.notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): IngredientsViewHolder {
        val view = LayoutInflater
            .from(this.context)
            .inflate(R.layout.ingredient_card_view, parent, false)
        return IngredientsViewHolder(view, this, this.context)
    }

    override fun getItemCount(): Int = this.myIngredients.size

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        holder.bind(this.myIngredients[position])
    }

    fun removeAt(position: Int) {
        this.ingredientsRef.document(this.myIngredients[position].id).delete()
    }

    fun showAll() {
        listenerRegistration?.remove()
        myIngredients.clear()
        notifyDataSetChanged()
        addListenerAll()
    }

    fun showFiltered(filter: String) {
        listenerRegistration?.remove()
        myIngredients.clear()
        notifyDataSetChanged()
        addListenerFiltered(filter)
    }

    fun selectIngredientAt(adapterPosition: Int) {
        val ingredientID = this.myIngredients[adapterPosition].id
        this.listener.onIngredientSelected(ingredientID)
    }
}