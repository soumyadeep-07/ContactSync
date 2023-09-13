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
import kotlinx.coroutines.withContext
import online.soumya.contactsync.adapter.MailActivityRecViewAdapter
import online.soumya.contactsync.databinding.ActivityMainBinding
import online.soumya.contactsync.model.MainActivityModel

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var Contacts:ArrayList<MainActivityModel>
    private lateinit var adapter: MailActivityRecViewAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var contactsRef: DatabaseReference
    private lateinit var query: Query
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Contacts = arrayListOf()
        binding.recViewContacts.layoutManager = LinearLayoutManager(this)
        binding.recViewContacts.setHasFixedSize(true)
        adapter = MailActivityRecViewAdapter(Contacts)
        binding.imgUserPic.setOnClickListener {
            startActivity(Intent(this@MainActivity,UserProfileActivity::class.java))
        }
        loadProfilePicture()
        lifecycleScope.launch {
            fatchContacts()
            binding.progressBar2.visibility = View.VISIBLE
        }
        adapter = MailActivityRecViewAdapter(Contacts)
        binding.recViewContacts.adapter = adapter

        binding.fabIcon.setOnClickListener {
            startActivity(Intent(this,CreateNewContactActivity::class.java))
            finish()
        }
        database = FirebaseDatabase.getInstance()
        contactsRef = database.getReference("user_data").child(Firebase.auth.currentUser!!.uid)

        // Initialize the query with a default value
        query = contactsRef.orderByChild("name").equalTo("")

        binding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               // TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // Update the query when the user types a letter
            }

            override fun afterTextChanged(p0: Editable?) {
                //TODO("Not yet implemented")
                val searchText = p0.toString()

                // If the search text is empty, reset the query
                if (searchText.isEmpty()) {
                    lifecycleScope.launch {
                        fatchContacts()
                    }
                } else {
                    query = contactsRef.orderByChild("name")
                        .startAt(searchText)
                        .endAt(searchText + "\uf8ff")
                }
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Handle the results here
                        // Iterate through the results and display them
                        if(dataSnapshot.exists()){
                            Contacts.clear()
                            for (snapshot in dataSnapshot.children) {
                                val searchcontact = snapshot.getValue(MainActivityModel::class.java)
                                searchcontact.let {
                                    if (it != null) {
                                        Contacts.add(it)
                                    }
//                                Log.d("data", it.toString())
                                    adapter.notifyDataSetChanged()
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }else{
                            Toast.makeText(this@MainActivity,"No data found",Toast.LENGTH_SHORT).show()
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle any errors that occur during the query
                        println("Error: ${databaseError.message}")
                    }
                })
            }

        })
    }
//    override fun onBackPressed() {
//        if(this@MainActivity == MainActivity()){
//            val alertDialog = AlertDialog.Builder(this)
//            alertDialog.setTitle("Confirm Exit")
//            alertDialog.setMessage("Are you sure you want to exit the app?")
//
//            alertDialog.setPositiveButton("Yes") { _, _ ->
//                // Perform any necessary cleanup or exit the app
//                finish() // Call super to actually exit the app
//            }
//
//            alertDialog.setNegativeButton("No") { dialog, _ ->
//                dialog.dismiss() // Dismiss the dialog and do nothing
//            }
//
//            alertDialog.show()
//        }else{
//            super.onBackPressed()
//        }
//
//    }

    private suspend fun fatchContacts() {
        withContext(Dispatchers.IO){
            Firebase.database.reference.child("user_data").child(Firebase.auth.currentUser!!.uid).addValueEventListener(object :
                ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                       Contacts.clear()
                        try{
                            for(items in snapshot.children){
                                val item = items.getValue(MainActivityModel::class.java)
                                binding.progressBar2.visibility = View.GONE
                                item?.let {
                                    Contacts.add(it)
//                                Log.d("data", it.toString())
                                   adapter.notifyDataSetChanged()
                                }
                                adapter.notifyDataSetChanged()
                            }
                            //Toast.makeText(this@MainActivity,Contacts.toString(),Toast.LENGTH_SHORT).show()
//                        cartItemList.addAll(cartItem)
                        }catch (e:Exception){
                            Log.e("myCart","${e.message}")
                        }
                    }else{
                        // Toast.makeText(requireContext(),"No Item Found",Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                        binding.txtNoDataFound.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    fun loadProfilePicture(){
        val databaseReference = FirebaseDatabase.getInstance().getReference("userInfo").child(
            Firebase.auth.currentUser!!.uid).child("0")
        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(MainActivityModel::class.java)
                if (user?.img?.isEmpty() == true){
                    Toast.makeText(this@MainActivity,"Upload Profile Picture",Toast.LENGTH_SHORT).show()
                    binding.imgUserPic.setImageResource(R.drawable.profile)
                }else{
                    Glide.with(this@MainActivity)
                        .load(user?.img)
                        .apply(RequestOptions.bitmapTransform(CircleCrop())).into(binding.imgUserPic)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}