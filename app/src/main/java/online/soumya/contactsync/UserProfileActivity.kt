//package online.soumya.contactsync
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//import android.os.Environment
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Toast
//import androidx.activity.result.ActivityResultLauncher
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.DataSource
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.load.engine.GlideException
//import com.bumptech.glide.load.resource.bitmap.CircleCrop
//import com.bumptech.glide.request.RequestListener
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.Target
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.ValueEventListener
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.FirebaseStorage
//import online.soumya.contactsync.databinding.ActivityUserProfileBinding
//import online.soumya.contactsync.model.Contact
//import online.soumya.contactsync.model.MainActivityModel
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//class UserProfileActivity : AppCompatActivity() {
//    private val binding by lazy {
//        ActivityUserProfileBinding.inflate(layoutInflater)
//    }
//    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
//    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
//
//    private val REQUEST_IMAGE_CAPTURE = 1
//    private val REQUEST_STORAGE_PERMISSION = 123
//    private var imageUri: Uri? = null
//    private var downloadUrl: String = ""
//
//    private val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo").child(
//        Firebase.auth.currentUser!!.uid).child("0")
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//        takePictureLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                imageUri?.let { displayImage(it) }
//            }
//        }
//
//        pickImageLauncher = registerForActivityResult(
//            ActivityResultContracts.StartActivityForResult()
//        ) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                imageUri = result.data?.data
//                imageUri?.let { displayImage(it) }
//            }
//        }
//
//
//
//        val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo").child(
//            Firebase.auth.currentUser!!.uid).child("0")
//        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val user = snapshot.getValue(MainActivityModel::class.java)
//                if (user?.img?.isEmpty() == true){
//                    Toast.makeText(this@UserProfileActivity,"Upload Profile Picture",Toast.LENGTH_SHORT).show()
//                    binding.imgProfileHome.setImageResource(R.drawable.profile)
//                }else{
//                    Glide.with(this@UserProfileActivity)
//                        .load(user?.img)
//                        .apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.imgProfileHome)
//                }
//                binding.txtContactName.text = user?.name
//                binding.txtContactMobileNo.text = user?.mobileNo
//                binding.txtContactEmailid.text = user?.email
//            }
//            override fun onCancelled(error: DatabaseError) {
//                //TODO("Not yet implemented")
//            }
//
//        })
//        binding.imgProfileHome.setOnClickListener {
//            showImageSelectionDialog()
//        }
//
//    }
//
//    private fun showImageSelectionDialog() {
//        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
//        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
//        builder.setTitle("Select an Option")
//        builder.setItems(options) { _, which ->
//            when (which) {
//                0 -> captureImage()
//                1 -> pickImage()
//                2 -> builder.create().dismiss()
//            }
//        }
//        builder.show()
//    }
//
//    private fun captureImage() {
//        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
//                return
//            }
//
//            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//            storageDir?.mkdirs()
//            val imageFile = File(storageDir, "JPEG_${timeStamp}.jpg")
//            imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)
//
//            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//            takePictureLauncher.launch(cameraIntent)
//        } else {
//            Toast.makeText(this, "No camera available on this device.", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun pickImage() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//            != PackageManager.PERMISSION_GRANTED) {
//            // Permission is not granted, request it
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                REQUEST_STORAGE_PERMISSION
//            )
//        } else {
//            // Permission is already granted, you can proceed with your storage-related tasks
//            // ...
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickImageLauncher.launch(intent)
//        }
//    }
//
//    private fun displayImage(imageUri: Uri) {
//        //Toast.makeText(this, "Image selected: $imageUri", Toast.LENGTH_SHORT).show()
//        Glide.with(this)
//            .load(imageUri)
//            .apply(RequestOptions.bitmapTransform(CircleCrop()))
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .skipMemoryCache(true)
//            .listener(object : RequestListener<Drawable> {
//                override fun onLoadFailed(
//                    e: GlideException?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    e?.printStackTrace()
//                    return false
//                }
//
//                override fun onResourceReady(
//                    resource: Drawable?,
//                    model: Any?,
//                    target: Target<Drawable>?,
//                    dataSource: DataSource?,
//                    isFirstResource: Boolean
//                ): Boolean {
//                    return false
//                }
//            })
//            .into(binding.imgProfileHome)
//        binding.progressBar4.visibility =View.VISIBLE
//        uploadImageAndSaveContact()
//    }
//    private fun uploadImageAndSaveContact() {
//        val storage = FirebaseStorage.getInstance()
//        val storageRef = storage.reference
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val imageName = "${Firebase.auth.currentUser!!.uid}_${timestamp}.jpg"
//        val imageRef = storageRef.child("images/$imageName")
//
//        val uploadTask = imageUri?.let { imageRef.putFile(it) }
//
//        if (uploadTask != null) {
//            uploadTask
//                .addOnSuccessListener { taskSnapshot ->
//                    // Upload successful, get the download URL
//                    imageRef.downloadUrl
//                        .addOnSuccessListener { downloadUri ->
//                            downloadUrl = downloadUri.toString()
//
//                            val contact = Contact(
//                                downloadUrl,
//                                binding.txtContactName.text.toString(),
//                                binding.txtContactMobileNo.text.toString(),
//                                binding.txtContactEmailid.text.toString()
//                            )
//
//                            // Save the contact to the database
//                            saveContactToDatabase(contact)
//                        }
//                        .addOnFailureListener { e ->
//                            // Handle download URL retrieval failure
//                            e.printStackTrace()
//                        }
//                }
//                .addOnFailureListener { e ->
//                    // Handle upload failure
//                    e.printStackTrace()
//                    Toast.makeText(this,"image Upload  not successful",Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//
//    private fun saveContactToDatabase(contact: Contact) {
//        databaseReference.setValue(contact).addOnSuccessListener {
//            binding.progressBar4.visibility = View.GONE
//                Toast.makeText(this, "Profile Update", Toast.LENGTH_SHORT).show()
//            }
//            ?.addOnFailureListener {
//                // Handle the error
//                // You can add error handling here
//                Toast.makeText(this, "Profile not Update", Toast.LENGTH_SHORT).show()
//            }
//    }
//}
package online.soumya.contactsync

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import online.soumya.contactsync.databinding.ActivityUserProfileBinding
import online.soumya.contactsync.model.Contact
import online.soumya.contactsync.model.MainActivityModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityUserProfileBinding.inflate(layoutInflater)
    }
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_STORAGE_PERMISSION = 123
    private var imageUri: Uri? = null
    private var downloadUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        takePictureLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri?.let { displayImage(it) }
            }
        }

        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                imageUri?.let { displayImage(it) }
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo").child(
            Firebase.auth.currentUser!!.uid).child("0")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(MainActivityModel::class.java)
                    if (user?.img.isNullOrEmpty()) {
                        Toast.makeText(
                            this@UserProfileActivity,
                            "Upload Profile Picture",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.imgProfileHome.setImageResource(R.drawable.profile)
                    } else {
                        Glide.with(this@UserProfileActivity)
                            .load(user?.img)
                            .apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.imgProfileHome)
                    }
                    binding.txtContactName.text = user?.name
                    binding.txtContactMobileNo.text = user?.mobileNo
                    binding.txtContactEmailid.text = user?.email
                } else {
                    Toast.makeText(this@UserProfileActivity, "No Data Found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                handleError(error.toException())
            }
        })

        binding.imgProfileHome.setOnClickListener {
            showImageSelectionDialog()
        }
    }

    private fun showImageSelectionDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Select an Option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> captureImage()
                1 -> pickImage()
                2 -> builder.create().dismiss()
            }
        }
        builder.show()
    }

    private fun pickImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            try {
                // Permission is already granted, you can proceed with your storage-related tasks
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickImageLauncher.launch(intent)
            } catch (e: Exception) {
                // Handle image picking error
                handleError(e)
            }
        }
    }

    private fun captureImage() {
        try {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
                    return
                }

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                storageDir?.mkdirs()
                val imageFile = File(storageDir, "JPEG_${timeStamp}.jpg")
                imageUri = FileProvider.getUriForFile(this, "${packageName}.provider", imageFile)

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                takePictureLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(this, "No camera available on this device.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // Handle image capture error
            handleError(e)
        }
    }

    private fun displayImage(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.printStackTrace()
                    Toast.makeText(this@UserProfileActivity,e!!.message.toString(),Toast.LENGTH_SHORT).show()
                    // Handle the image loading error here, if needed
                    // For example, display a toast message or show a placeholder image
//                    if (e != null) {
//                        handleError(e)
//                    }
                    binding.progressBar4.visibility = View.GONE // Hide the progress bar
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Image loading was successful
                    // Proceed with the upload operation
                    uploadImageAndSaveContact()
                    return false
                }
            })
            .into(binding.imgProfileHome)
        binding.progressBar4.visibility = View.VISIBLE
    }

    private fun uploadImageAndSaveContact() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageName = "${Firebase.auth.currentUser!!.uid}_${timestamp}.jpg"
        val imageRef = storageRef.child("images/$imageName")

        val uploadTask = imageUri?.let { imageRef.putFile(it) }

        uploadTask?.addOnSuccessListener { taskSnapshot ->
            // Upload successful, get the download URL
            imageRef.downloadUrl
                .addOnSuccessListener { downloadUri ->
                    downloadUrl = downloadUri.toString()

                    val contact = Contact(
                        downloadUrl,
                        binding.txtContactName.text.toString(),
                        binding.txtContactMobileNo.text.toString(),
                        binding.txtContactEmailid.text.toString()
                    )

                    // Save the contact to the database
                    saveContactToDatabase(contact)
                }
                .addOnFailureListener { e ->
                    // Handle download URL retrieval failure
                    handleError(e)
                }
        }?.addOnFailureListener { e ->
            // Handle upload failure
            handleError(e)
        }
    }

    private fun saveContactToDatabase(contact: Contact) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo")
            .child(Firebase.auth.currentUser!!.uid).child("0")

        try {
            databaseReference.setValue(contact).addOnSuccessListener {
                binding.progressBar4.visibility = View.GONE
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                // Handle the error
                handleError(e)
            }
        } catch (e: Exception) {
            // Handle other exceptions
            handleError(e)
        }
    }

    private fun handleError(exception: Throwable) {
        // Handle the error appropriately
        exception.printStackTrace()
        Toast.makeText(this, "An error occurred: ${exception.message}", Toast.LENGTH_SHORT).show()
    }
}