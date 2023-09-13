package online.soumya.contactsync

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import online.soumya.contactsync.databinding.ActivityLoginBinding
import online.soumya.contactsync.model.MainActivityModel
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private val userData = ArrayList<MainActivityModel>()
    private var phoneNumber: String = ""
    private var name: String = ""
    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        verificationId = ""
        binding.progressBar3.visibility = View.GONE

        binding.btnSendOtp.setOnClickListener {
            phoneNumber = binding.edtMobileNumber.text.toString()
            name = binding.edtName.text.toString()
            email = binding.edtEmailid.text.toString()
            if (name.isNotBlank() && email.isNotBlank() && isValidPhoneNumber(phoneNumber) && phoneNumber.length == 10) {
                sendVerificationCode("+91 $phoneNumber")
            } else {
                if (name.isBlank()) {
                    // Handle name validation error, e.g., show an error message for missing name
                    Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                } else if (email.isBlank()) {
                    // Handle email validation error, e.g., show an error message for missing email
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                } else if (phoneNumber.isBlank()) {
                    // Handle phone number validation error, e.g., show an error message for missing phone number
                    Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle phone number validation error, e.g., show an error message for invalid phone number
                    Toast.makeText(this, "Please enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.outlinedButton.setOnClickListener {
            binding.progressBar3.visibility = View.VISIBLE
            val code = binding.edtOtp.text.toString()
            verifyPhoneNumberWithCode(verificationId, code)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in.
            startActivity(Intent(this@Login, MainActivity::class.java))
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    try {
                        signInWithPhoneAuthCredential(credential)
                    } catch (e: Exception) {
                        shoeToast(e.message.toString())
                        // Handle the exception, e.g., log an error or show a message to the user
                        e.printStackTrace()
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    try {
                        if (e is FirebaseTooManyRequestsException) {
                            // Handle the case where too many requests were made
                            shoeToast(e.message.toString())
                        } else {
                            shoeToast("Try After some Time")
                            // Handle other verification failures
                        }
                    } catch (e: Exception) {
                        // Handle the exception, e.g., log an error or show a message to the user
                        shoeToast(e.message.toString())
                        e.printStackTrace()
                    }
                }

                override fun onCodeSent(
                    sentVerificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    try {
                        verificationId = sentVerificationId
                    } catch (e: Exception) {
                        shoeToast(e.message.toString())
                        // Handle the exception, e.g., log an error or show a message to the user
                        e.printStackTrace()
                    }
                }
            })
            .build()

        try {
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            shoeToast(e.message.toString())
            // Handle the exception, e.g., log an error or show a message to the user
            e.printStackTrace()
        }
    }


    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }

//    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
//        val credential = PhoneAuthProvider.getCredential(verificationId, code)
//        signInWithPhoneAuthCredential(credential)
//    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        } catch (e: IllegalArgumentException) {
            // Handle the specific error where PhoneAuthCredential cannot be created
            // Show an error message to the user
            shoeToast("Error: Invalid PhoneAuthCredential")
            binding.progressBar3.visibility = View.GONE
//            Toast.makeText(this, "Error: Invalid PhoneAuthCredential", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle other exceptions that may occur during the authentication process
            // Show an error message to the user
            shoeToast("Authentication failed: ${e.message.toString()}")
            binding.progressBar3.visibility = View.GONE
//            Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        FirebaseAuth.getInstance().signInWithCredential(credential)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    // Authentication was successful, handle it here
//                    val user = task.result?.user
//                    // You can access the authenticated user with 'user'
//                } else {
//                    // Authentication failed, handle the error here
//                    val exception = task.exception
//                    // Show an error message to the user or handle the error as needed
//                    Toast.makeText(this, "Authentication failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                if (authResult.user?.uid != null) {
                    val database = FirebaseDatabase.getInstance()
                    val uid = Firebase.auth.currentUser?.uid
                    userData.add(MainActivityModel("", name, phoneNumber, email))
                    val databaseReference = database.getReference("userInfo").child(uid!!)
                    databaseReference.setValue(userData)
                        ?.addOnSuccessListener {
                            runOnUiThread {
                                binding.progressBar3.visibility = View.GONE
                                startActivity(Intent(this@Login, MainActivity::class.java))
                                finish()
                            }
                        }
                        ?.addOnFailureListener { e ->
                            runOnUiThread {
                                // Handle database write failure
                                shoeToast("Contact is not Saved: ${e.message}")
                                binding.progressBar3.visibility = View.GONE
                            }
                        }
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                runOnUiThread {
                    // Handle invalid verification code
                    shoeToast("Invalid verification code")
                    binding.progressBar3.visibility = View.GONE
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                runOnUiThread {
                    // Handle invalid user (e.g., user doesn't exist)
                    shoeToast("Invalid user")
                    binding.progressBar3.visibility = View.GONE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    // Handle other exceptions
                    shoeToast("Sign-In failed: ${e.message.toString()}")
                    binding.progressBar3.visibility = View.GONE
                }
            }
        }
    }

    fun shoeToast(message:String){
        Toast.makeText(this@Login,message,Toast.LENGTH_SHORT).show()
    }
}
