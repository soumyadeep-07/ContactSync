package online.soumya.contactsync

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import online.soumya.contactsync.databinding.ActivityProfileBinding
import online.soumya.contactsync.model.MainActivityModel

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var mobileNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val intent = intent
        mobileNumber = intent.getStringExtra("mobileNo").toString()

        binding.imgPopUp.setOnClickListener {
            showPopupMenu()
        }
        binding.imgBack.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
            finish()
        }

        runBlocking {
            fetchContactData()
        }
    }

    private suspend fun fetchContactData() {
        withContext(Dispatchers.IO) {
            try {
                val databaseReference = FirebaseDatabase.getInstance().getReference("user_data")
                    .child(Firebase.auth.currentUser!!.uid)

                val query = databaseReference.orderByChild("mobileNo").equalTo(mobileNumber)

                val snapshot = query.get().await()

                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val contact = childSnapshot.getValue(MainActivityModel::class.java)
                        contact?.let {
                            setContactData(it)
                        }
                    }
                } else {
                    showToast("Contact not found for mobile number: $mobileNumber")
                }
            } catch (e: Exception) {
                // Log the exception to help with debugging
                Log.e("FetchContactDataError", "Error fetching contact data: ${e.message}", e)

                // Show a toast or handle the error as needed
                showToast("Error fetching contact data: ${e.message}")
            }
        }
    }



    private fun setContactData(contact: MainActivityModel) {
        runOnUiThread {
            Glide.with(this@ProfileActivity)
                .load(contact.img)
                .apply(RequestOptions.bitmapTransform(CircleCrop()))
                .into(binding.imgProfileHome)
            binding.txtContactName.text = contact.name
            binding.txtContactMobileNo.text = contact.mobileNo
            binding.txtContactEmailid.text = contact.email
        }
    }

    private fun showPopupMenu() {
        val popupMenu = PopupMenu(this, binding.imgPopUp)
        popupMenu.inflate(R.menu.pop_up_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_contact -> {
                    // Handle edit action
                    // Launch an edit activity or fragment for the contact
                    true
                }
                R.id.delete_contact -> {
                    // Handle delete action
                    // Delete the contact from the database
                    deleteContact()
                    true
                }
                R.id.share_contact -> {
                    // Handle share action
                    // Share contact information via intent
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun deleteContact() {
        val builder = AlertDialog.Builder(this)

        // Set dialog title and message
        builder.setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")

        // Add positive button with a click listener
        builder.setPositiveButton("Delete") { dialog, which ->
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("user_data")
                    .child(Firebase.auth.currentUser!!.uid)
                    .orderByChild("mobileNo").equalTo(mobileNumber)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val childKey = snapshot.key
                        if (childKey != null) {
                            //databaseReference.child(childKey.toString()).removeValue()
                            //showToast("Contact deleted")
                            finish()
                        }
                    }
                }

//                databaseReference.orderByChild("mobileNo").equalTo(mobileNumber).addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()) {
//                            for (contactSnapshot in snapshot.children) {
//                                val contactKey = contactSnapshot.key
//                                if (contactKey != null) {
//                                    val contactRef = databaseReference.child(contactKey)
//                                    contactRef.removeValue()
//                                    showToast("Contact deleted")
//                                    finish()
//                                }
//                            }
//                        }
//                    }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Contact deletion failed")
                }
            })
        }

        // Add negative button with a click listener
        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle the negative button click (optional)
        }

        // Create and show the dialog
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@ProfileActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
