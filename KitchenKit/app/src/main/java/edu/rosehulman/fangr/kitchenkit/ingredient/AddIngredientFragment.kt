package edu.rosehulman.fangr.kitchenkit.ingredient

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import edu.rosehulman.fangr.kitchenkit.Utils
import kotlinx.android.synthetic.main.add_ingredient_view.view.*
import kotlinx.android.synthetic.main.add_ingredient_view.view.name_text_spinner
import kotlinx.android.synthetic.main.fragment_my_ingredients.view.*
import java.lang.NumberFormatException
import java.lang.RuntimeException

const val ARG_ADD_UID = "uid_add"

/**
 * A simple [Fragment] subclass.
 * Use the [AddIngredientFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddIngredientFragment(private var ingredientList: ArrayList<String>) : Fragment() {

    private var uid: String = ""

    //    private var ingredient: Ingredient? = null
    private var listener: OnAddButtonPressedListener? = null
    lateinit var ingredientName: String
    lateinit var ingredientUnit: String
    private lateinit var rootView: View
    private var nameSpinner: Spinner? = null
    private var unitSpinner: Spinner? = null
    private lateinit var nameSpinnerAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.uid = it.getString(ARG_ADD_UID).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_add_ingredient, container, false)
        initializeIngNameSpinner()
        initializeUnitSpinner()

        this.rootView.button_add.setOnClickListener {
            val name = this.ingredientName
            if (name.isEmpty()) {
                Toast.makeText(this.context, "Name cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val amount: Double
            try {
                amount = this.rootView.amount_edit_text.text.toString().toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this.context, "Invalid number input", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val unit = this.ingredientUnit
            val isFrozen = this.rootView.checkBox.isChecked

            val ingredientsReference = FirebaseFirestore
                .getInstance()
                .collection(Constants.USER_COLLECTION)
                .document(this.uid)
                .collection(Constants.INGREDIENT_COLLECTION)
            ingredientsReference.add(Ingredient(name, amount, isFrozen, unit))

            this.listener?.onAddButtonPressed()
        }

        this.rootView.button_back.setOnClickListener {
            this.listener?.onAddIngredientFragmentBackButtonPressed()
        }

        this.rootView.button_customize_one.setOnClickListener {
            this.listener?.onCustomizeIngredientButtonPressed()
        }

        return this.rootView
    }

    private fun initializeIngNameSpinner() {
        this.nameSpinner = rootView.name_text_spinner
        this.nameSpinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, ingredientList)
        this.nameSpinner?.adapter = nameSpinnerAdapter
        this.nameSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d(Constants.TAG, "Spinner nothing selected")
                return
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val content: String = parent?.getItemAtPosition(position).toString()
                Log.d(Constants.TAG, "item selected: $content")
                when (parent?.id) {
                    R.id.name_text_spinner -> {
                        ingredientName = content
                    }
                }
                val url: String? = Utils.getIngUrlFromName(ingredientName)
                Log.d(Constants.TAG, "ingredient url found: $url")
                Picasso.get().load(url).into(rootView.food_image)
            }
        }
    }

    private fun initializeUnitSpinner() {
        this.unitSpinner = rootView.amount_unit_spinner
        this.unitSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d(Constants.TAG, "Spinner nothing selected")
                return
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val content: String = parent?.getItemAtPosition(position).toString()
                Log.d(Constants.TAG, "item selected: $content")
                when (parent?.id) {
                    R.id.amount_unit_spinner -> {
                        ingredientUnit = content
                    }
                }
            }
        }
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
         * @param uid the unique ID of the current user
         * @return A new instance of fragment AddIngredientFragment.
         */
        @JvmStatic
        fun newInstance(
            uid: String,
            ingredientList: ArrayList<String>
        ) = AddIngredientFragment(ingredientList).apply {
            this.arguments = Bundle().apply {
                this.putString(ARG_ADD_UID, uid)
            }
        }
    }

    interface OnAddButtonPressedListener {
        fun onAddButtonPressed()
        fun onAddIngredientFragmentBackButtonPressed()
        fun onCustomizeIngredientButtonPressed()
    }
}