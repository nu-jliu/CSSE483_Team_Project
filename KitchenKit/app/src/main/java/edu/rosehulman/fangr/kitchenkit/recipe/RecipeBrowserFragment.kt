package edu.rosehulman.fangr.kitchenkit.recipe

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_recipe_browser.view.*
import kotlinx.android.synthetic.main.recipe_card_recycler.view.*
import java.util.*
import kotlin.collections.ArrayList

const val ARG_UID_RECIPE = "uid_recipe"

class RecipeBrowserFragment : Fragment(), TabLayout.OnTabSelectedListener {

    private var uid: String? = null
    private var buttonPressedListener: OnButtonPressedListener? = null
    private var adapter: RecipeAdapter? = null
    private var favoriteReference: CollectionReference? = null
    private val categories = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.arguments?.let {
            this.uid = it.getString(ARG_UID_RECIPE)
        }
        Log.d(Constants.TAG, "UID = ${this.uid}")
        this.favoriteReference =
            this.uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(it)
                    .collection(Constants.FAVORITES_COLLECTION)
            }

        this.favoriteReference?.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
            if (exception != null) {
                Log.e(Constants.TAG, "EXCEPTION: $exception")
                return@addSnapshotListener
            }
            for (document in snapshot!!) {
                val category = document
                    .data[Constants.KEY_CATEGORY]
                    .toString()
                    .toLowerCase(Locale.ROOT)
                this.categories.add(category)
            }
        }
        Log.d(Constants.TAG, "favorite: ${this.categories}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_browser, container, false)

        view.recipe_recycler_view.layoutManager = LinearLayoutManager(this.context)
        this.adapter = this.context?.let { context ->
            this.buttonPressedListener?.let { listener ->
                RecipeAdapter(
                    context,
                    Constants.VALUE_ALL,
                    listener,
                    this.uid
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
            this.buttonPressedListener?.onRecipeSearchButtonPressed(this.adapter)
        }

        view.button_recommend.setOnClickListener {
            if (this.adapter?.category != Constants.VALUE_ALL)
                return@setOnClickListener
            val showRecommended =
                this.buttonPressedListener?.onShowRecommendedButtonPressed(this.adapter)
            if (showRecommended!!)
                (it as ImageButton).setImageResource(R.drawable.ic_recommand_red)
            else
                (it as ImageButton).setImageResource(R.drawable.ic_recommend_recipes)
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

    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab reselected ${tab?.text}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab unselected ${tab?.text}")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(Constants.TAG, "Tab selected ${tab?.text}")
        this.adapter = when (tab?.text) {
            context?.getString(R.string.all) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_ALL,
                        listener,
                        this.uid
                    )
                }
            }
            context?.getString(R.string.dinner) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_DINNER,
                        listener,
                        this.uid
                    )
                }
            }
            context?.getString(R.string.vegan) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_VEGAN,
                        listener,
                        this.uid
                    )
                }
            }
            context?.getString(R.string.snack) -> context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_SNACK,
                        listener,
                        this.uid
                    )
                }
            }
            this.context?.getString(R.string.like) -> this.context?.let {
                this.buttonPressedListener?.let { listener ->
                    RecipeAdapter(
                        it,
                        Constants.VALUE_LIKE,
                        listener,
                        this.uid
                    )
                }
            }
            else -> null
        }
        this.view?.recipe_recycler_view?.adapter = this.adapter
        this.view?.button_recommend?.setImageResource(R.drawable.ic_recommend_recipes)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uid the unique id of the current user
         * @return A new instance of fragment RecipeBrowserFragment.
         */
        @JvmStatic
        fun newInstance(uid: String) = RecipeBrowserFragment().apply {
            this.arguments = Bundle().apply {
                this.putString(ARG_UID_RECIPE, uid)
            }
        }
    }

    interface OnButtonPressedListener {
        fun onProfileButtonPressed()
        fun onMyIngredientsButtonPressed()
        fun onRecipeSearchButtonPressed(adapter: RecipeAdapter?)
        fun onRecipeSelected(recipeID: String)
        fun onShowRecommendedButtonPressed(adapter: RecipeAdapter?): Boolean?
    }
}