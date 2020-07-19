package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.my_ingredients_page.view.*
import java.lang.RuntimeException

class RecipeBrowserFragment : Fragment() {

    private var buttonPressedListener: OnButtonPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recipe_browser, container, false)
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