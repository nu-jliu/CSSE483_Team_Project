package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import java.lang.RuntimeException

const val ARG_EDIT_UID = "uid_edit"

class EditProfileFragment : Fragment() {

    private var uid: String? = null
    private var profileReference: CollectionReference? = null
    private var information: Information? = null
    private var listener: OnButtonsPressedListener? = null
    private var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uid = this.arguments?.getString(ARG_EDIT_UID)

        this.profileReference =
            this.uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(it)
                    .collection(Constants.PROFILE_COLLECTION)
            }

        this.profileReference?.document(Constants.USER_INFO_DOCUMENT)?.get()
            ?.addOnSuccessListener { snapshot: DocumentSnapshot? ->
                this.information = snapshot?.let { Information.fromSnapshot(it) }
                this.rootView?.name_edit_text_view?.setText(this.information?.name)
                this.rootView?.cooked_year_edit_text_view?.setText(this.information?.year.toString())
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        view.button_save.setOnClickListener {
            val name = view.name_edit_text_view.text.toString()
            val year = view.cooked_year_edit_text_view.text.toString().toInt()
            this.information?.name = name
            this.information?.year = year
            this.information?.let {
                this.profileReference
                    ?.document(Constants.USER_INFO_DOCUMENT)
                    ?.set(it)
            }
            this.listener?.onButtonsPressed()
        }

        view.button_edit_profile_back.setOnClickListener {
            this.listener?.onButtonsPressed()
        }

        this.rootView = view
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonsPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnSaveButtonPressedListener")
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param uid unique ID of the current user
         * @return A new instance of fragment EditProfileFragment.
         */
        @JvmStatic
        fun newInstance(uid: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    this.putString(ARG_EDIT_UID, uid)
                }
            }
    }

    interface OnButtonsPressedListener {
        fun onButtonsPressed()
    }
}