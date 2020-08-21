package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_recipe_detail.view.*

const val ARG_RECIPE_ID = "recipe_id"

class RecipeDetailFragment : Fragment() {

    private var rootView: View? = null
    private var listener: OnButtonPressedListener? = null
    private var recipeID: String? = null

    private val recipeReference = FirebaseFirestore
        .getInstance()
        .collection(Constants.RECIPE_COLLECTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.recipeID = it.getString(ARG_RECIPE_ID)
        }

        this.recipeReference.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
            if (exception != null) {
                Log.e(Constants.TAG, "EXCEPTION: $exception")
                return@addSnapshotListener
            }
            for (document in snapshot!!) {
                val recipe = Recipe.fromSnapshot(document)
                if (recipe.id == recipeID) {
                    this.rootView?.recipe_title?.text = recipe.name
                    var detail = recipe.ingredient + "\n" + recipe.procedure
                    detail = System.getProperty("line.separator")
                        ?.let { detail.replace("\\n", it) }
                        .toString()
                    this.rootView?.recipe_detail?.text = detail
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false)

        this.rootView?.button_back?.setOnClickListener {
            this.listener?.onRecipeDetailFragmentBackButtonPressed()
        }

        return this.rootView
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
         * @param recipeID the unique ID of the recipe
         * @return A new instance of fragment RecipeDetailFragment.
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