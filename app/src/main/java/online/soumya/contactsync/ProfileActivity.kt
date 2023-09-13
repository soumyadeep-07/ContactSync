package online.soumya.contactsync


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import online.soumya.contactsync.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var mobileNumber:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val intent = intent
        mobileNumber = intent.getStringExtra("mobileNo").toString()

        binding.imgPopUp.setOnClickListener {
            showPopupMenu()
        }
        binding.imgBack.setOnClickListener {
            startActivity(Intent(this@ProfileActivity,MainActivity::class.java))
            finish()
        }
        val databaseReference = FirebaseDatabase.getInstance().getReference("user_data").child(Firebase.auth.currentUser!!.uid)
        databaseReference.orderByChild("mobileNo").equalTo(mobileNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        // This loop will iterate over matching contacts
                        //val contact = snapshot.getValue(MainActivityModel::class.java)
                        // Process the contact data as needed
                        Glide.with(this@ProfileActivity).load(snapshot.child("img").value.toString()).apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.imgProfileHome)
                        binding.txtContactName.text = snapshot.child("name").value.toString()
                        binding.txtContactMobileNo.text = snapshot.child("mobileNo").value.toString()
                        binding.txtContactEmailid.text = snapshot.child("email").value.toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle any errors
                }

            })
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

    fun deleteContact(){
        val builder = AlertDialog.Builder(this)

        // Set dialog title and message
        builder.setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete this contact?")

        // Add positive button with a click listener
        builder.setPositiveButton("Delete") { dialog, which ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("user_data").child(Firebase.auth.currentUser!!.uid)
            databaseReference.orderByChild("mobileNo").equalTo(mobileNumber).addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val childKey = snapshot.key
                        if (childKey != null) {
                            databaseReference.child(childKey).removeValue()
                            Toast.makeText(this@ProfileActivity, "Contact deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Contact Not deleted", Toast.LENGTH_SHORT).show()
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
}