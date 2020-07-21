package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.lang.RuntimeException

const val ARG_NAME = "name"

class ProfileFragment : Fragment() {

    private var name: String = "Names"
    private var listener: ProfileFragment.OnLogoutPressedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.name = this.arguments?.getString(ARG_NAME).toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        view.button_logout.setOnClickListener {
            this.listener?.onLogoutPressed()
        }
        view.name_text_view.text = name
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLogoutPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnLogoutPressedListener")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(name: String) = ProfileFragment().apply {
            this.arguments = Bundle().apply {
                this.putString(ARG_NAME, name)
            }
        }
    }

    interface OnLogoutPressedListener {
        fun onLogoutPressed()
    }
}