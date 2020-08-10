package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_recipe_detail.view.*
import java.lang.RuntimeException

const val ARG_RECIPE_ID = "recipe_id"

class RecipeDetailFragment : Fragment() {

    private var rootView: View? = null
    private var listener: OnButtonPressedListener? = null
    private var recipeID: String? = null

    private val recipeRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.RECIPE_COLLECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.recipeID = it.getString(ARG_RECIPE_ID)
        }

        recipeRef.addSnapshotListener { snapshot: QuerySnapshot?, e ->
            if (e != null) {
                Log.e(Constants.TAG, "EXCEPTION: $e")
                return@addSnapshotListener
            }
            for (document in snapshot!!) {
                val recipe = Recipe.fromSnapshot(document                )
                if (recipe.id == recipeID) {
                    rootView?.recipe_title?.text = recipe.name
                    val detail = recipe.ingredient + "\n" + recipe.procedure
                    rootView?.recipe_detail?.text = detail
                }
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false)
        rootView?.button_back?.setOnClickListener {
            this.listener?.onRecipeDetailFragmentBackButtonPressed()
        }

        return rootView
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnButtonPressedListener")
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
        fun newInstance(recipeID: String) =
            RecipeDetailFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARG_RECIPE_ID, recipeID)
                }
            }
    }

    interface OnButtonPressedListener {
        fun onRecipeDetailFragmentBackButtonPressed()
    }
}