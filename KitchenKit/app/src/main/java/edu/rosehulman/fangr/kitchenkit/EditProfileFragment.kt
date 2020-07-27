package edu.rosehulman.fangr.kitchenkit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*

const val ARG_EDIT_UID = "uid_edit"

class EditProfileFragment : Fragment() {

    private var uid: String? = null
    private var profileReference: CollectionReference? = null
    private var information: Information? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            this.uid = it.getString(ARG_EDIT_UID)
        }
        this.profileReference =
            this.uid?.let {
                FirebaseFirestore
                    .getInstance()
                    .collection(Constants.USER_COLLECTION)
                    .document(it)
                    .collection(Constants.PROFILE_COLLECTION)
            }
        val informationReference = this.profileReference?.document(Constants.USER_INFO_DOCUMENT)
        informationReference?.get()?.addOnSuccessListener { snapshot: DocumentSnapshot? ->
            this.information = snapshot?.let { Information.fromSnapshot(it) }!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

//        this.information?.name?.let { view.name_edit_text_view.setText(it) }
//        this.information?.year?.let { view.cooked_year_edit_text_view.setText(it) }

        view.name_edit_text_view.setText("name")
        view.cooked_year_edit_text_view.setText("3")


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
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uid: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    this.putString(ARG_EDIT_UID, uid)
                }
            }
    }
}