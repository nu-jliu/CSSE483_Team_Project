package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.add_ingredient_view.view.*
import kotlinx.android.synthetic.main.fragment_my_ingredients.view.*
import java.lang.NumberFormatException
import java.lang.RuntimeException

const val ARG_ADD_UID = "uid_add"
const val ARG_INGREDIENT = "ingredient"

/**
 * A simple [Fragment] subclass.
 * Use the [AddIngredientFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddIngredientFragment : Fragment() {

    private var uid: String = ""
    private var ingredient: Ingredient? = null
    private var listener: OnAddButtonPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.uid = it.getString(ARG_ADD_UID).toString()
//            this.ingredient = it.getParcelable(ARG_INGREDIENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_ingredient, container, false)
        view.button_add.setOnClickListener {
            val name = view.name_edit_text.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this.context, "Name cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val amount: Double
            try {
                amount = view.amount_edit_text.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this.context, "Invalid number input", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val isFrozen = view.checkBox.isChecked

            val ingredientsReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.USER_COLLECTION)
                .document(this.uid)
                .collection(Constants.INGREDIENT_COLLECTION)
            ingredientsReference.add(Ingredient(name, amount, isFrozen))

            this.listener?.onAddButtonPressed()
        }

        view.button_back.setOnClickListener {
            this.listener?.onAddIngredientFragmentBackButtonPressed()
        }

        view.button_customize_one.setOnClickListener {
            this.listener?.onCustomizeIngredientButtonPressed()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAddButtonPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnAddButtonPressedListener")
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
         *@param uid the unique ID of the current user
         * @return A new instance of fragment AddIngredientFragment.
         */
        @JvmStatic
        fun newInstance(uid: String) =
            AddIngredientFragment().apply {
                arguments = Bundle().apply {
                    this.putString(ARG_ADD_UID, uid)
                }
            }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         *@param uid the unique ID of the current user
         * @return A new instance of fragment AddIngredientFragment.
         */
        @JvmStatic
        fun newInstance(uid: String, ingredient: Ingredient) =
            AddIngredientFragment().apply {
                arguments = Bundle().apply {
                    this.putString(ARG_ADD_UID, uid)
                    this.putParcelable(ARG_INGREDIENT, ingredient)
                }
            }
    }

    interface OnAddButtonPressedListener {
        fun onAddButtonPressed()
        fun onAddIngredientFragmentBackButtonPressed()
        fun onCustomizeIngredientButtonPressed()
    }
}