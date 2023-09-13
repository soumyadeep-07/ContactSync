package online.soumya.contactsync

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import online.soumya.contactsync.adapter.MailActivityRecViewAdapter
import online.soumya.contactsync.databinding.ActivityMainBinding
import online.soumya.contactsync.model.MainActivityModel
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var contacts: ArrayList<MainActivityModel>
    private lateinit var adapter: MailActivityRecViewAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var contactsRef: DatabaseReference
    private lateinit var query: Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        contacts = arrayListOf()
        binding.recViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recViewContacts.setHasFixedSize(true)
        adapter = MailActivityRecViewAdapter(contacts)
        lifecycleScope.launch {
            fetchContacts()
        }
        binding.imgUserPic.setOnClickListener {
            startActivity(Intent(this@MainActivity, UserProfileActivity::class.java))
        }
        loadProfilePicture()
        binding.progressBar2.visibility = View.VISIBLE
        binding.recViewContacts.adapter = adapter

        binding.fabIcon.setOnClickListener {
            startActivity(Intent(this, CreateNewContactActivity::class.java))
            finish()
        }

        database = FirebaseDatabase.getInstance()
        contactsRef = database.getReference("user_data").child(Firebase.auth.currentUser!!.uid)

        // Initialize the query with a default value
        query = contactsRef.orderByChild("name").equalTo("")

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Not used
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Update the query when the user types a letter
                val searchText = p0.toString()

                // If the search text is empty, reset the query
                if (searchText.isEmpty()) {
                    lifecycleScope.launch {
                        fetchContacts()
                    }
                } else {
                    query = contactsRef.orderByChild("name")
                        .startAt(searchText)
                        .endAt(searchText + "\uf8ff")
                }

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            contacts.clear()
                            for (snapshot in dataSnapshot.children) {
                                val searchContact = snapshot.getValue(MainActivityModel::class.java)
                                searchContact?.let {
                                    contacts.add(it)
                                }
                            }
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this@MainActivity, "No data found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("DatabaseError", "Error: ${databaseError.message}")
                    }
                })
            }
            override fun afterTextChanged(p0: Editable?) {
                // Not used
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun fetchContacts() {
        withContext(Dispatchers.IO) {
            try {
                val snapshot = Firebase.database.reference.child("user_data")
                    .child(Firebase.auth.currentUser!!.uid).get().await()
                if (snapshot.exists()) {
                    val newContacts = ArrayList<MainActivityModel>() // Create a new list

                    for (item in snapshot.children) {
                        val contact = item.getValue(MainActivityModel::class.java)
                        contact?.let {
                            newContacts.add(it) // Add each contact to the new list
                        }
                    }

                    // Replace the old contacts list with the new one
                    contacts.clear()
                    contacts.addAll(newContacts)

                    // Notify the adapter of the data change
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        binding.progressBar2.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Contacts loaded successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        binding.progressBar2.visibility = View.GONE
                        binding.txtNoDataFound.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                Log.e("FetchContactsError", "${e.message}")
            }
        }
    }



    private fun loadProfilePicture() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo")
            .child(Firebase.auth.currentUser!!.uid).child("0")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(MainActivityModel::class.java)
                if (user?.img?.isEmpty() == true) {
                    Toast.makeText(this@MainActivity, "Upload Profile Picture", Toast.LENGTH_SHORT).show()
                    binding.imgUserPic.setImageResource(R.drawable.profile)
                } else {
                    Glide.with(this@MainActivity)
                        .load(user?.img)
                        .apply(RequestOptions.bitmapTransform(CircleCrop()))
                        .into(binding.imgUserPic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfilePictureError", "Error: ${error.message}")
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
