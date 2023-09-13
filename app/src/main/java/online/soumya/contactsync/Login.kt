package online.soumya.contactsync

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import online.soumya.contactsync.databinding.ActivityLoginBinding
import online.soumya.contactsync.model.MainActivityModel
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private val binding:ActivityLoginBinding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var userData:ArrayList<MainActivityModel>
    var phoneNumber:String =""
    var name:String = ""
    var email:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        verificationId = ""
        userData = arrayListOf()
        binding.btnSendOtp.setOnClickListener {
            phoneNumber = binding.edtMobileNumber.text.toString()
            name = binding.edtName.text.toString()
            email = binding.edtEmailid.text.toString()
            if (isValidPhoneNumber(phoneNumber) && name.isNotEmpty() && email.isNotEmpty() && phoneNumber.isNotEmpty()) {
                sendVerificationCode("+91 $phoneNumber")
            } else {
                Toast.makeText(this,"invalid phone number",Toast.LENGTH_SHORT).show()
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
            startActivity(Intent(this@Login,MainActivity::class.java))
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (e is FirebaseTooManyRequestsException) {
                        // Handle the case where too many requests were made
                    } else {
                        // Handle other verification failures
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@Login.verificationId = verificationId
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val authResult = auth.signInWithCredential(credential).await()
                if(authResult.user?.uid != null){
                    val database = FirebaseDatabase.getInstance()
                    val uid = Firebase.auth.currentUser?.uid
                    userData.add(MainActivityModel("",name,phoneNumber,email))
                    val databaseReference = database.getReference("userInfo").child(uid!!)
                    databaseReference.setValue(userData)
                        ?.addOnSuccessListener {
                            binding.progressBar3.visibility = View.GONE
                            startActivity(Intent(this@Login,MainActivity::class.java))
                            finish()
                            //Toast.makeText(this@Login, "Contact is Saved", Toast.LENGTH_SHORT).show()
                        }
                        ?.addOnFailureListener {
                            // Handle the error
                            // You can add error handling here
                            Toast.makeText(this@Login, "Contact is not Saved", Toast.LENGTH_SHORT).show()
                        }
                }
                // Handle successful sign-in
            } catch (e: Exception) {
                // Handle sign-in failure
                Toast.makeText(this@Login,"${e.toString()}",Toast.LENGTH_SHORT).show()
            }
        }
    }
}