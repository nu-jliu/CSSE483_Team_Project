package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_add_ingredient.view.*
import kotlinx.android.synthetic.main.add_ingredient_view.view.*
import java.lang.RuntimeException

const val ARG_EDIT_ING_UID = "edit_ing_uid"
const val ARG_INGREDIENT_ID = "ingredient_id"

class EditIngredientFragment : Fragment() {

    private var uid: String? = null
    private var ingredientID: String? = null
    private var ingredientReference: DocumentReference? = null
    private var ingredient: Ingredient? = null
    private var rootView: View? = null
    private var listener: OnIngredientSaveButtonPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.uid = it.getString(ARG_EDIT_ING_UID)
            this.ingredientID = it.getString(ARG_INGREDIENT_ID)
        }

        this.ingredientReference = this.ingredientID?.let {
            this.uid?.let { uid ->
                FirebaseFirestore.getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(uid)
                    .collection(Constants.INGREDIENT_COLLECTION)
                    .document(it)
            }
        }

        this.ingredientReference?.get()?.addOnSuccessListener {snapshot: DocumentSnapshot? ->
            this.ingredient = snapshot?.let { Ingredient.fromSnapshot(it) }
            this.rootView?.amount_edit_text?.setText(this.ingredient?.amount.toString())
            this.rootView?.checkBox?.isChecked = this.ingredient?.isFrozen ?: false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.rootView = inflater.inflate(R.layout.fragment_add_ingredient, container, false)
        this.rootView?.button_add?.text = "SAVE"

        this.rootView?.button_add?.setOnClickListener {
            this.ingredient?.amount = this.rootView!!.amount_edit_text.text.toString().toDouble()
            this.ingredient?.isFrozen = this.rootView!!.checkBox.isChecked

            this.ingredient?.let { data -> this.ingredientReference?.set(data) }

            this.listener?.onSaveButtonPressed()
        }

        this.rootView?.button_back?.setOnClickListener {
            this.listener?.onSaveButtonPressed()
        }

        return this.rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnIngredientSaveButtonPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnIngredientSaveButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uid the unique ID of the current user
         * @return A new instance of fragment AddIngredientFragment.
         */
        @JvmStatic
        fun newInstance(uid: String, ingredientID: String) =
            EditIngredientFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARG_EDIT_ING_UID, uid)
                    this.putString(ARG_INGREDIENT_ID, ingredientID)
                }
            }
    }

    interface OnIngredientSaveButtonPressedListener {
        fun onSaveButtonPressed()
    }
}