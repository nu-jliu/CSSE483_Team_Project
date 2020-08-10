package edu.rosehulman.fangr.kitchenkit

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

class StoredIngredient (
    var name: String = "",
    var url: String = "",
    var expire1: String = "",
    var expire2: String = "",
    var expireFrozen1: String = "",
    var expireFrozen2: String = "",
    var canFroze: Boolean = false
){
    @get:Exclude
    var id = ""

    companion object {
        fun fromSnapshot(snapshot: DocumentSnapshot): StoredIngredient {
            val storedIngredient = snapshot.toObject(StoredIngredient::class.java)!!
            storedIngredient.id = snapshot.id
            return storedIngredient
        }
    }
}