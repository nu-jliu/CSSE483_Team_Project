package edu.rosehulman.fangr.kitchenkit.profile

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

data class Information(
    var name: String = "",
    var year: Int = 0,
    var photoUrl: String = ""
) {

    @get:Exclude
    var id = ""

    companion object {

        fun fromSnapshot(snapshot: DocumentSnapshot): Information {
            val information = snapshot.toObject(Information::class.java)!!
            information.id = snapshot.id
            return information
        }
    }
}