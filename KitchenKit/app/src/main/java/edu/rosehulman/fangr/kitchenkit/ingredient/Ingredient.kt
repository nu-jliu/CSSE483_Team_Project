package edu.rosehulman.fangr.kitchenkit.ingredient

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredient(
    var name: String = "",
    var amount: Double = 0.0,
    var isFrozen: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    @get:Exclude
    var id = ""

    @IgnoredOnParcel
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