package edu.rosehulman.fangr.kitchenkit.ingredient

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_my_ingredients.view.*
import kotlinx.android.synthetic.main.recipe_card_recycler.view.*
import java.lang.RuntimeException

const val ARG_UID = "uid"

class MyIngredientsFragment : Fragment() {

    private var uid: String? = null
    private var listener: OnButtonPressedListener? = null
    private var adapter: IngredientsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uid = this.arguments?.getString(ARG_UID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_ingredients, container, false)
        val ingNameArr = initializeIngNameArray()
        view.recipe_recycler_view.layoutManager = LinearLayoutManager(this.context)
        this.adapter = this.context?.let {
            this.uid?.let { uid ->
                this.listener?.let { listener ->
                    IngredientsAdapter(uid, it, view.recipe_recycler_view, listener)
                }
            }
        }
        view.recipe_recycler_view.adapter = this.adapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter?.removeAt(viewHolder.adapterPosition)
            }

        }).attachToRecyclerView(view.recipe_recycler_view)

        view.button_back.setOnClickListener {
            this.listener?.onMyIngredientsFragmentBackButtonPressed()
        }

        view.fab_my_ingredients.setOnClickListener {
            this.listener?.onAddFABPressed(ingNameArr)
        }

        view.button_search.setOnClickListener {
            this.listener?.onIngredientSearchButtonPressed(this.adapter)
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonPressedListener)
            this.listener = context
        else
            throw  RuntimeException("$context must implement OnBackButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

    private fun initializeIngNameArray(): ArrayList<String> {
        val ingsRef = FirebaseFirestore.getInstance().collection("storedIngredient")
        val nameArr = ArrayList<String>()
        ingsRef.get().addOnSuccessListener { snapshot: QuerySnapshot ->
            for (doc in snapshot) {
                val name = doc["name"].toString()
                nameArr.add(name)
            }
        }
        return nameArr
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uid the unique ID of the current user
         * @return A new instance of fragment MyRecipeFragment.
         */
        fun newInstance(uid: String) =
            MyIngredientsFragment().apply {
                this.arguments = Bundle().apply {
                    this.putString(ARG_UID, uid)
                }
            }
    }

    interface OnButtonPressedListener {
        fun onMyIngredientsFragmentBackButtonPressed()
        fun onAddFABPressed(ingredientList: ArrayList<String>)
        fun onIngredientSearchButtonPressed(adapter: IngredientsAdapter?)
        fun onIngredientSelected(ingredientID: String)
    }

}