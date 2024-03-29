package edu.rosehulman.fangr.kitchenkit.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_profile.view.*

const val ARG_UID_PROFILE = "uid_profile"

class ProfileFragment : Fragment() {

    private var uid: String? = null
    private var listener: OnButtonsPressedListener? = null

    private var profileReference: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uid = this.arguments?.getString(ARG_UID_PROFILE)
        this.profileReference =
            this.uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(it)
                    .collection(Constants.PROFILE_COLLECTION)
            }
    }

    @ExperimentalStdlibApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        this.profileReference?.document(Constants.USER_INFO_DOCUMENT)
            ?.addSnapshotListener { snapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "EXCEPTION: $exception")
                    return@addSnapshotListener
                }
                if (snapshot?.data == null) {
                    val newInformation = Information("user@${this.uid}", 0, "")
                    newInformation.id = Constants.USER_INFO_DOCUMENT
                    this.profileReference!!
                        .document(Constants.USER_INFO_DOCUMENT)
                        .set(newInformation)
                    return@addSnapshotListener
                }
                val information = Information.fromSnapshot(snapshot)
                Log.d(Constants.TAG, "User information: $information")
                view.name_text_view.text = information.name
                view.heading_text_view.text = this.context?.resources?.getQuantityString(
                    R.plurals.title_user_info,
                    information.year,
                    information.year
                ) ?: this.context?.getString(R.string.default_user_info_title)
                if (information.photoUrl == "")
                    view.avatar_image_view.setImageResource(R.drawable.default_avatar)
                else
                    Picasso
                        .get()
                        .load(information.photoUrl)
                        .into(view.avatar_image_view)
            }

        view.favorites_recycler_view.layoutManager = LinearLayoutManager(this.context)
        val adapter = this.context?.let { this.uid?.let { id -> FavoritesAdapter(it, id) } }
        view.favorites_recycler_view.adapter = adapter

        view.fab_add_favorite.setOnClickListener {
            adapter?.showAddDialog()
        }

        view.button_logout.setOnClickListener {
            this.listener?.onLogoutPressed()
        }

        view.button_profile_back.setOnClickListener {
            this.listener?.onProfileBackPressed()
        }

        view.button_edit_profile.setOnClickListener {
            this.listener?.onEditButtonPressed()
        }

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
         * @param uid unique ID of the current user
         * @return A new instance of fragment ProfileFragment.
         */
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