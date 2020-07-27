package edu.rosehulman.fangr.kitchenkit

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.heading_text_view
import kotlinx.android.synthetic.main.fragment_profile.view.name_text_view
import java.lang.RuntimeException

const val ARG_UID_PROFILE = "uid_profile"

class ProfileFragment : Fragment() {

    private var uid: String? = null
    private var listener: OnButtonsPressedListener? = null
    private lateinit var rootView: View

    private var profileRef: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uid = this.arguments?.getString(ARG_UID_PROFILE)
        this.profileRef =
            this.uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(it)
                    .collection(Constants.PROFILE_COLLECTION)
            }
        val informationRef = this.profileRef?.document(Constants.USER_INFO_DOCUMENT)
        informationRef?.addSnapshotListener { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->
            if (exception != null) {
                Log.e(Constants.TAG, "EXCEPTION: $exception")
                return@addSnapshotListener
            }
            val information = snapshot?.let { Information.fromSnapshot(it) }
            Log.d(Constants.TAG, "User information: $information")
            this.rootView.name_text_view.text = information?.name
            this.rootView.heading_text_view.text =
                information?.year
                    ?.let {
                        this.context?.resources?.getQuantityString(
                            R.plurals.title_user_info,
                            it,
                            it
                        )
                    }
                    ?: this.context?.getString(R.string.default_user_info_title)
        }
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

        view.button_profile_back.setOnClickListener {
            this.listener?.onProfileBackPressed()
        }

        view.button_edit_profile.setOnClickListener {
            this.listener?.onEditButtonPressed()
        }

        this.rootView = view
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonsPressedListener)
            this.listener = context
        else
            throw RuntimeException("$context must implement OnLogoutPressedListener")
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
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment2.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uid: String) = ProfileFragment().apply {
            this.arguments = Bundle().apply {
                this.putString(ARG_UID_PROFILE, uid)
            }
        }
    }

    interface OnButtonsPressedListener {
        fun onLogoutPressed()
        fun onProfileBackPressed()
        fun onEditButtonPressed()
    }
}