package edu.rosehulman.fangr.kitchenkit.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import edu.rosehulman.fangr.kitchenkit.BitmapUtils
import edu.rosehulman.fangr.kitchenkit.Constants
import edu.rosehulman.fangr.kitchenkit.R
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

const val ARG_EDIT_UID = "uid_edit"

private const val RC_TAKE_PICTURE = 1
private const val RC_CHOOSE_PICTURE = 2

class EditProfileFragment : Fragment() {

    private var uid: String? = null
    private var profileReference: CollectionReference? = null
    private lateinit var storageReference: StorageReference
    private var information: Information? = null
    private var listener: OnButtonsPressedListener? = null
    private var rootView: View? = null
    private var currentPhotoPath = ""

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

        this.storageReference = FirebaseStorage.getInstance().reference.child("images")

        this.profileReference?.document(Constants.USER_INFO_DOCUMENT)?.get()
            ?.addOnSuccessListener { snapshot: DocumentSnapshot? ->
                this.information = snapshot?.let { Information.fromSnapshot(it) }
                this.rootView?.name_edit_text_view?.setText(this.information?.name)
                this.rootView?.cooked_year_edit_text_view?.setText(this.information?.year.toString())
                if (this.information?.photoUrl == "")
                    this.rootView?.edit_avatar_image?.setImageResource(R.drawable.default_avatar)
                else
                    Picasso
                        .get()
                        .load(this.information?.photoUrl)
                        .into(this.rootView?.edit_avatar_image)
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

        view.button_upload_avatar.setOnClickListener {
            this.launchEditProfileAvatarDialog()
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

    private fun updateAvatar(location: String) {
        ImageRescaleTask(location).execute()
    }

    private fun launchEditProfileAvatarDialog() {
        val builder = AlertDialog.Builder(this.context)

        builder.setTitle("Upload New Avatar Photo")
        builder.setMessage("Would you like to take a picture or choose an existing one?")

        builder.setPositiveButton("Take Picture") { _, _ ->
            this.launchCameraIntent()
        }
        builder.setNegativeButton("Choose Picture") { _, _ ->
            this.launchChooseIntent()
        }

        builder.create().show()
    }

    // Everything camera- and storage-related is from
    // https://developer.android.com/training/camera/photobasics
    private fun launchCameraIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(this.requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    this.createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    // authority declared in manifest
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this.requireContext(),
                        "edu.rosehulman.fangr.kitchenkit",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    this.startActivityForResult(takePictureIntent, RC_TAKE_PICTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File =
            this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = this.absolutePath
        }
    }

    private fun launchChooseIntent() {
        // https://developer.android.com/guide/topics/providers/document-provider
        val choosePictureIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        choosePictureIntent.addCategory(Intent.CATEGORY_OPENABLE)
        choosePictureIntent.type = "image/*"
        if (choosePictureIntent.resolveActivity(this.requireContext().packageManager) != null)
            this.startActivityForResult(choosePictureIntent, RC_CHOOSE_PICTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RC_TAKE_PICTURE -> this.sendCameraPhotoToPage()
                RC_CHOOSE_PICTURE -> this.sendGalleryPhotoToPage(data)
            }
        }
    }

    private fun sendCameraPhotoToPage() {
        this.addPhotoToGallery()
        Log.d(Constants.TAG, "Sending Photo to Profile Page: ${this.currentPhotoPath}")
        this.updateAvatar(this.currentPhotoPath)
    }

    private fun sendGalleryPhotoToPage(data: Intent?) {
        if (data != null && data.data != null) {
            val location = data.data.toString()
            this.updateAvatar(location)
        }
    }

    // Works Not working on phone
    @Suppress("DEPRECATION")
    private fun addPhotoToGallery() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(this.currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            this.requireActivity().sendBroadcast(mediaScanIntent)
        }
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
                this.arguments = Bundle().apply {
                    this.putString(ARG_EDIT_UID, uid)
                }
            }
    }

    interface OnButtonsPressedListener {
        fun onButtonsPressed()
    }

    @SuppressLint("StaticFieldLeak")
    inner class ImageRescaleTask(private val localPath: String) : AsyncTask<Void, Void, Bitmap>() {

        @RequiresApi(Build.VERSION_CODES.N)
        override fun doInBackground(vararg params: Void?): Bitmap? {
            val ratio = 2
            return context?.let { BitmapUtils.rotateAndScaleByRatio(it, this.localPath, ratio) }
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            storageAdd(this.localPath, bitmap)
        }
    }

    private fun storageAdd(localPath: String, bitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask = this.uid?.let { this.storageReference.child(it).putBytes(data) }

        uploadTask?.addOnFailureListener {
            Log.d(Constants.TAG, "Image upload failed: $localPath ${this.uid}")
        }?.addOnSuccessListener {
            Log.d(Constants.TAG, "Image upload succeeded: $localPath ${this.uid}")
        }

        uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task: Task<UploadTask.TaskSnapshot> ->
            if (!task.isSuccessful)
                task.exception?.let { throw it }
            return@Continuation this.uid?.let { this.storageReference.child(it).downloadUrl }
        })?.addOnCompleteListener { task: Task<Uri> ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                this.information?.photoUrl = downloadUri.toString()
                this.information?.let {
                    this.profileReference
                        ?.document(Constants.USER_INFO_DOCUMENT)
                        ?.set(it)
                }
                Log.d(Constants.TAG, "Image download successful: $downloadUri")
            } else
                Log.d(Constants.TAG, "Image download failed")
        }
    }
}