package online.soumya.contactsync

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import online.soumya.contactsync.databinding.ActivityCreateNewContactBinding
import java.io.File
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.request.target.Target
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import online.soumya.contactsync.model.Contact

class CreateNewContactActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityCreateNewContactBinding.inflate(layoutInflater)
    }

    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_STORAGE_PERMISSION = 123
    private var imageUri: Uri? = null
    private var downloadUrl: String = ""

    private val database = FirebaseDatabase.getInstance()
    private val uid = Firebase.auth.currentUser?.uid
    private val databaseReference = uid?.let {
        database.getReference("user_data").child(it)
    }

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

        binding.imgUpload.setOnClickListener {
            showImageSelectionDialog()
        }

        binding.imgCancil.setOnClickListener {
            openMainActivity()
        }

        binding.imgSaveContact.setOnClickListener {
            if (binding.edtName.text.toString().isNotEmpty() &&
                binding.edtEmail.text.toString().isNotEmpty() &&
                binding.edtMobileNumber.text.toString().isNotEmpty()
            ) {
                uploadImageAndSaveContact()
            }
        }
    }

    private fun openMainActivity() {
        startActivity(Intent(this@CreateNewContactActivity,MainActivity::class.java))
        finish()
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

    private fun captureImage() {
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
            // Permission is already granted, you can proceed with your storage-related tasks
            // ...
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    private fun displayImage(imageUri: Uri) {
        //Toast.makeText(this, "Image selected: $imageUri", Toast.LENGTH_SHORT).show()
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
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(binding.imgUpload)
    }

    private fun uploadImageAndSaveContact() {
        binding.transpreant.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageName = "${uid}_${timestamp}.jpg"
        val imageRef = storageRef.child("images/$imageName")

        val uploadTask = imageUri?.let { imageRef.putFile(it) }

        if (uploadTask != null) {
            uploadTask
                .addOnSuccessListener { taskSnapshot ->
                    // Upload successful, get the download URL
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            downloadUrl = downloadUri.toString()

                            val contact = Contact(
                                downloadUrl,
                                binding.edtName.text.toString(),
                                binding.edtMobileNumber.text.toString(),
                                binding.edtEmail.text.toString()
                            )

                            // Save the contact to the database
                            saveContactToDatabase(contact)
                        }
                        .addOnFailureListener { e ->
                            // Handle download URL retrieval failure
                            e.printStackTrace()
                        }
                }
                .addOnFailureListener { e ->
                    // Handle upload failure
                    e.printStackTrace()
                    Toast.makeText(this,"image Upload  not successful",Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveContactToDatabase(contact: Contact) {
        databaseReference?.push()?.setValue(contact)
            ?.addOnSuccessListener {
                binding.transpreant.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Contact is Saved", Toast.LENGTH_SHORT).show()
                openMainActivity()
            }
            ?.addOnFailureListener {
                // Handle the error
                // You can add error handling here
                Toast.makeText(this, "Contact is not Saved", Toast.LENGTH_SHORT).show()
            }
    }
}