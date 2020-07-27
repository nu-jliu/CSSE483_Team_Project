package edu.rosehulman.fangr.kitchenkit

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Ingredient(
    var name: String = "",
    var amount: Double = 0.0,
    var isFrozen: Boolean = false
) {

    @get:Exclude
    var id = ""

    @ServerTimestamp
    var bought: Timestamp? = null

    companion object {

        const val BOUGHT_KEY = "bought"

        fun fromSnapshot(snapshot: DocumentSnapshot): Ingredient {
            val ingredient = snapshot.toObject(Ingredient::class.java)!!
            ingredient.id = snapshot.id
            return ingredient
        }

    }
}