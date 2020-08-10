package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_recipe_browser.view.*
import kotlinx.android.synthetic.main.my_ingredients_page.view.*
import kotlinx.android.synthetic.main.my_ingredients_page.view.button_profile
import kotlinx.android.synthetic.main.my_ingredients_page.view.fab_my_ingredients
import kotlinx.android.synthetic.main.recipe_card_recycler.view.*
import java.lang.RuntimeException

class RecipeBrowserFragment : Fragment() {

    private var buttonPressedListener: OnButtonPressedListener? = null
    private val recipeReference = FirebaseFirestore.getInstance().collection(Constants.RECIPE_COLLECTION)

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
        view.recipe_recycler_view.adapter = this.context?.let { RecipeAdapter(it, Constants.VALUE_DINNER) }

        view.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d(Constants.TAG, "Tab reselected ${tab?.text}")
//                this.onTabSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.d(Constants.TAG, "Tab unselected ${tab?.text}")
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.d(Constants.TAG, "Tab selected ${tab?.text}")
                view.recipe_recycler_view.adapter = when (tab?.text) {
                    context?.getString(R.string.dinner) -> context?.let { RecipeAdapter(it, Constants.VALUE_DINNER) }
                    context?.getString(R.string.asian) -> context?.let { RecipeAdapter(it, Constants.VALUE_ASIAN) }
                    context?.getString(R.string.mexican) -> context?.let { RecipeAdapter(it, Constants.VALUE_MEXICAN) }
                    context?.getString(R.string.vegan) -> context?.let { RecipeAdapter(it, Constants.VALUE_VEGAN) }
                    else -> null
                }
            }
        })

        view.button_profile.setOnClickListener {
            this.buttonPressedListener?.onProfileButtonPressed()
        }
        view.fab_my_ingredients.setOnClickListener {
            this.buttonPressedListener?.onMyIngredientsButtonPressed()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonPressedListener)
            this.buttonPressedListener = context
        else
            throw RuntimeException("$context must implement OnProfileButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        this.buttonPressedListener = null
    }

    interface OnButtonPressedListener {
        fun onProfileButtonPressed()
        fun onMyIngredientsButtonPressed()
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