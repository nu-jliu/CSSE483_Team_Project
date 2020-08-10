package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_recipe_browser.view.*
import kotlinx.android.synthetic.main.recipe_card_recycler.view.*
import java.lang.RuntimeException

class RecipeBrowserFragment : Fragment(), TabLayout.OnTabSelectedListener {

    var buttonPressedListener: OnButtonPressedListener? = null
    private val recipeReference =
        FirebaseFirestore.getInstance().collection(Constants.RECIPE_COLLECTION)
    private var adapter: RecipeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_browser, container, false)

        view.recipe_recycler_view.layoutManager = LinearLayoutManager(this.context)
        this.adapter = this.context?.let {
            this.buttonPressedListener?.let { listener ->
                RecipeAdapter(
                    it,
                    Constants.VALUE_ALL,
                    listener
                )
            }
        }
        view.recipe_recycler_view.adapter = this.adapter

        view.tabs.addOnTabSelectedListener(this)

        view.button_profile.setOnClickListener {
            this.buttonPressedListener?.onProfileButtonPressed()
        }

        view.fab_my_ingredients.setOnClickListener {
            this.buttonPressedListener?.onMyIngredientsButtonPressed()
        }
        view.button_search.setOnClickListener {
            this.buttonPressedListener?.onRecipeSearchButtonPressed(adapter)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonPressedListener)
            this.buttonPressedListener = context
        else
            throw RuntimeException("$context must implement OnButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        this.buttonPressedListener = null
    }

    interface OnButtonPressedListener {
        fun onProfileButtonPressed()
        fun onMyIngredientsButtonPressed()
        fun onRecipeSearchButtonPressed(adapter: RecipeAdapter?)
        fun onRecipeSelected(recipeID: String)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab reselected ${tab?.text}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab unselected ${tab?.text}")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab selected ${tab?.text}")
        requireView().recipe_recycler_view.adapter = when (tab?.text) {
            context?.getString(R.string.all) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_ALL,
                        listener
                    )
                }
            }
            context?.getString(R.string.dinner) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_DINNER,
                        listener
                    )
                }
            }
            context?.getString(R.string.vegan) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_VEGAN,
                        listener
                    )
                }
            }
            context?.getString(R.string.snack) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_SNACK,
                        listener
                    )
                }
            }
            else -> null
        }
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment RecipeBrowserFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance() {
//
//        }
//    }
}