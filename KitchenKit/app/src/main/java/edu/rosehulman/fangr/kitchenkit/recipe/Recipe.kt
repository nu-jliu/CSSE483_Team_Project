package edu.rosehulman.fangr.kitchenkit.recipe

import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude
import edu.rosehulman.fangr.kitchenkit.Constants
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Recipe(
    var name: String = "",
    var amountCal: Int = 0,
    var amountIng: Int = 0,
    var amountTime: Int = 0,
    var ingredient: String = "",
    var procedure: String = "",
    var url: String = "",
    var ingArray: List<String> = ArrayList(),
    var category: ArrayList<String> = arrayListOf("all")
//    var category: String = ""
) : Parcelable {

    @IgnoredOnParcel
    @get:Exclude
    var id = ""

    companion object {
        const val NAME_KEY = "name"
        fun fromSnapshot(snapshot: DocumentSnapshot): Recipe {
            Log.d(Constants.TAG, "$snapshot")
            val recipe = snapshot.toObject(Recipe::class.java)!!
            recipe.id = snapshot.id
            return recipe
        }
    }
}