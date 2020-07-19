package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_splash.view.*
import java.lang.RuntimeException

class SplashFragment : Fragment() {

    private var listener: OnSignInButtonPressedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        view.button_sign_in.setOnClickListener {
            this.listener?.onSignInButtonPressed()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSignInButtonPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnSignInButtonPressedListener")
    }

    override fun onDetach() {
        super.onDetach()
        this.listener = null
    }

    interface OnSignInButtonPressedListener {
        fun onSignInButtonPressed()
    }
}