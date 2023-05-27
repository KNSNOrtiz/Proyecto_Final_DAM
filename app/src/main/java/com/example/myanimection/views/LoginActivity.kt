package com.example.myanimection.views

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val userController = UserController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val resultGoogleLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        FirebaseAuth.getInstance().signInWithCredential(credential)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    //  Comprobación de si es el primer inicio de sesión para darlo de alta en la BD. Se hace aquí
                                    //  para evitar hacer lecturas continuamente.
                                    val additionalInfo = it.result.additionalUserInfo
                                    if (additionalInfo != null) {
                                        if (additionalInfo.isNewUser) {
                                            val user = FirebaseAuth.getInstance().currentUser
                                            userController.isUserRegistered(user!!.uid, object:FirestoreQueryCallback {
                                                override fun onQueryComplete(success: Boolean) {
                                                    if (!success) {
                                                        userController.addUser(User(user.uid, user.displayName!!, user.email!!, arrayListOf()))
                                                    }
                                                }
                                                override fun onQueryFailure(exception: Exception) {
                                                    Log.e("User query failed", "${exception.message}")
                                                }
                                            })
                                        }
                                    }
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Notifications.shortToast(
                                        this,
                                        "No se pudo iniciar sesión con Google."
                                    )
                                }
                            }.addOnFailureListener {
                                Notifications.shortToast(
                                    this,
                                    "No se pudo iniciar sesión con Google."
                                )
                            }
                    }
                }
            }


        var isPassVisible = false

        val btnLogIn : Button = findViewById(R.id.btnLogin)
        val btnGoogleLogIn: Button = findViewById(R.id.btnGoogleLogIn)
        val txtSignUp : TextView = findViewById(R.id.txtSignUp)
        val txtEmailSign : EditText = findViewById(R.id.txtEmailSignin)
        val txtPasswordSign : EditText = findViewById(R.id.txtPasswordSignin)
        val txtForgotPassword: TextView = findViewById(R.id.txtForgotPassword)
        val btnViewPassword : ImageButton = findViewById(R.id.btnViewPassword)



        supportActionBar?.hide()

        txtSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        btnLogIn.setOnClickListener { signIn(txtEmailSign.text.toString().trim(), txtPasswordSign.text.toString().trim()) }

        btnGoogleLogIn.setOnClickListener {
            googleSignIn(resultGoogleLauncher)
        }
        
        txtForgotPassword.setOnClickListener {
            val intent = Intent(this, PasswordRecoverActivity::class.java)
            startActivity(intent)
        }

        btnViewPassword.setOnClickListener() {
            if (!isPassVisible){
                txtPasswordSign.transformationMethod = null
                txtPasswordSign.setSelection(txtPasswordSign.text.length)
                isPassVisible = true
            } else{
                txtPasswordSign.transformationMethod = PasswordTransformationMethod()
                txtPasswordSign.setSelection(txtPasswordSign.text.length)
                isPassVisible = false
            }
        }
    }


    //  INICIAR SESIÓN CON EMAIL/PASSWORD.
    private fun signIn(email:String, password: String){
        if (validateFields(email, password)){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val user = it.result.user
                    if (user != null){
                        if (user.isEmailVerified) {
                            var intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else{
                            Notifications.alertDialogOK(this, "Cuenta no verificada",
                                "Por favor, verifica la cuenta mediante el enlace en tu email para poder iniciar sesión.")
                        }
                    }
                } else{
                    Notifications.shortToast(this, "El usuario especificado no existe.")
                }
            }
        }
    }

    private fun googleSignIn(launcher: ActivityResultLauncher<Intent>) {
        val conf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        val client = GoogleSignIn.getClient(this, conf)
        client.signOut()

        launcher.launch(client.signInIntent)
    }

    private fun validateFields(email:String, password:String) : Boolean{
        if (email.isEmpty() || password.isEmpty()){
            Notifications.shortToast(this, "Los campos están vacíos.")
            return false
        }
        return true
    }


}