package com.example.myanimection.views

import android.annotation.SuppressLint
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
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.example.myanimection.utils.Utilities
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val userController = UserController()
    private lateinit var mainActivityIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mainActivityIntent = Intent(this, MainActivity::class.java)

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
                                    val user = FirebaseAuth.getInstance().currentUser
                                    //  Comprobación de si es el primer inicio de sesión para darlo de alta en la BD. Se hace aquí
                                    //  para evitar hacer lecturas continuamente.
                                    val additionalInfo = it.result.additionalUserInfo
                                    if (additionalInfo != null) {
                                        if (additionalInfo.isNewUser) {
                                            userController.isUserRegistered(user!!.uid, object:FirestoreQueryCallback {
                                                override fun onQueryComplete(success: Boolean) {
                                                    if (!success) {
                                                        userController.addUser(User(user.uid, "", user.email!!, arrayListOf(), ""), object: FirestoreQueryCallback{
                                                            override fun onQueryComplete(success: Boolean) {
                                                                Notifications.shortToast(this@LoginActivity, "Registro completado.")
                                                                checkUsername(user.uid)
                                                            }
                                                            override fun onQueryFailure(exception: Exception) {
                                                                Notifications.shortToast(this@LoginActivity, "Hubo un error en el registro.")
                                                                Log.e("LOGIN", exception.message.toString())
                                                            }
                                                        })
                                                    }
                                                }
                                                override fun onQueryFailure(exception: Exception) {
                                                    Log.e("User query failed", "${exception.message}")
                                                }
                                            })
                                        }
                                    }
                                    userController.getUserName(user!!.uid, object: UserController.StringQueryCallback {
                                        override fun onQueryComplete(result: String) {
                                            if (result.isNotEmpty()) {
                                                startActivity(mainActivityIntent)
                                            } else {
                                                checkUsername(user.uid)
                                            }
                                        }
                                        override fun onQueryFailure(exception: Exception) {
                                            Notifications.shortToast(this@LoginActivity, "Error en el inicio de sesión.")
                                            Log.e("LOGIN GOOGLE", exception.message.toString() )
                                        }
                                    })
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
                            startActivity(mainActivityIntent)
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

    //  Método que fuerza a los usuarios de Google a tener un nombre de usuario antes de poder iniciar sesión y acceder al resto de la aplicación.
    @SuppressLint("CheckResult")
    private fun checkUsername(uid: String) {
        val context = this@LoginActivity
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        MaterialDialog(context).show {
            title(text = "Establecer nombre de usuario")
            input(hint = "Nombre de usuario (5-15 caracteres)") { dialog, text ->
                val userName = text.toString().trim()
                if (userName.matches(Utilities.USERNAME_REGEX)) {
                    userController.isUsernameTaken(userName, object: FirestoreQueryCallback {
                        override fun onQueryComplete(success: Boolean) {
                            if (success) {
                                Notifications.shortToast(context, "Nombre de usuario no disponible."
                                )
                            } else {
                                userController.setUsername(uid, userName, object: FirestoreQueryCallback {
                                    override fun onQueryComplete(success: Boolean) {
                                        if (success) {
                                            dialog.dismiss()
                                            Notifications.shortToast(context, "Bienvenido/a, $userName." )
                                            startActivity(mainActivityIntent)
                                        } else {
                                            Notifications.shortToast(context, "Error al actualizar el usuario. Inténtalo de nuevo.")
                                        }
                                    }
                                    override fun onQueryFailure(exception: Exception) {
                                        Notifications.shortToast(context, "Error al actualizar el usuario. Inténtalo de nuevo."
                                        )
                                    }
                                })
                            }
                        }
                        override fun onQueryFailure(exception: Exception) {
                            Notifications.shortToast(context, "No se pudo recuperar el usuario."
                            )
                        }
                    })
                } else {
                    Notifications.shortToast(context, "Solo se permiten 5-15 caracteres alfanuméricos sin espacios.")
                }
            }
            positiveButton(text = "Aceptar")
            negativeButton(text = "Cancelar", click = { dialog ->
                dialog.dismiss()
            })
        }
    }


    private fun validateFields(email:String, password:String) : Boolean{
        if (email.isEmpty() || password.isEmpty()){
            Notifications.shortToast(this, "Los campos están vacíos.")
            return false
        }
        return true
    }


}