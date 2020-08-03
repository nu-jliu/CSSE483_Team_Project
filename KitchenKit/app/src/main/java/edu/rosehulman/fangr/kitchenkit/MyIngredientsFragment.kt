package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_my_ingredients.view.*
import kotlinx.android.synthetic.main.recipe_card_recycler.view.*
import java.lang.RuntimeException

const val ARG_UID = "uid"

class MyIngredientsFragment : Fragment() {

    private var uid: String? = null
    private var listener: OnButtonPressedListener? = null
    var adapter: IngredientsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uid = this.arguments?.getString(ARG_UID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_ingredients, container, false)

        view.recycler_view.layoutManager = LinearLayoutManager(this.context)
        adapter = this.context?.let {
            this.uid?.let { uid ->
                IngredientsAdapter(
                    uid,
                    it,
                    view.recycler_view
                )
            }
        }
        view.recycler_view.adapter = adapter

        view.button_back.setOnClickListener {
            this.listener?.onMyIngredientsFragmentBackButtonPressed()
        }

        view.fab_my_ingredients.setOnClickListener {
            this.listener?.onAddFABPressed()
        }

        view.button_search.setOnClickListener{
            this.listener?.onIngredientSearchButtonPressed(adapter)
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
        fun onAddFABPressed()
        fun onIngredientSearchButtonPressed(adapter: IngredientsAdapter?)
    }

}