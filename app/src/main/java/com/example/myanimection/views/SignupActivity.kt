package com.example.myanimection.views

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.DialogCallback
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.example.myanimection.utils.Utilities.USERNAME_REGEX
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var txtConfirmPassword : EditText
    private var userController = UserController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        val btnSignup: Button = findViewById(R.id.btnSignup)
        val btnViewPassword: ImageButton = findViewById(R.id.btnViewPasswordSignUp)
        val txtEmail : TextView = findViewById(R.id.txtEmailSignUp)
        val txtPassword : EditText = findViewById(R.id.txtPasswordSignUp)
        val txtUsername : EditText = findViewById(R.id.txtUsernameSignUp)
        txtConfirmPassword = findViewById(R.id.txtConfirmPasswordSignUp)


        var isPassVisible = false

        btnSignup.setOnClickListener {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()
            val username = txtUsername.text.toString().trim()
            signUp(username, email, password) }

        //  Ver/Ocultar contraseña.
        btnViewPassword.setOnClickListener {
            if (!isPassVisible){
                txtPassword.transformationMethod = null
                txtConfirmPassword.transformationMethod = null
                isPassVisible = true
            } else{
                txtPassword.transformationMethod = PasswordTransformationMethod()
                txtConfirmPassword.transformationMethod = PasswordTransformationMethod()
                isPassVisible = false
            }
        }

        }
    /**  Método encargado de dar de alta usuarios mediante mediante email y contraseña.
     * @param username  Nombre de usuario con el que se reconocerá al usuario en la aplicación.
     * @param email     Email del usuario.
     * @param password  Contraseña del usuario.
     * */
    private fun signUp(username:String, email:String, password:String){
        if (validateFields(username, email,password)){
            //  Alta del usuario en Firebase Authentication.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val user = it.result.user
                    userController.isUsernameTaken(username, object: FirestoreQueryCallback {
                        override fun onQueryComplete(success: Boolean) {
                            if (!success && user != null) {
                                userController.addUser(User(user.uid, username, user.email!!, arrayListOf(), username.lowercase()), object: FirestoreQueryCallback{
                                    override fun onQueryComplete(success: Boolean) {
                                        //  En versiones más recientes se debe usar el Dispatcher para volver atrás en la pila de actividades o vistas.
                                        val dispatcher = onBackPressedDispatcher
                                        //  Correo de verificación.
                                        user.sendEmailVerification()
                                        Notifications.alertDialogOK(this@SignupActivity, "Usuario creado.", "Se ha enviado " +
                                                "un enlace de verificación a tu email. Verífica tu usuario para completar el registro.",
                                        positiveButtonClickListener = { dispatcher.onBackPressed() }, negativeButtonClickListener = { dispatcher.onBackPressed() })

                                    }
                                    override fun onQueryFailure(exception: Exception) {
                                        Notifications.shortToast(this@SignupActivity, "Hubo un error en el registro.")
                                    }
                                })
                            } else {
                                Notifications.shortToast(this@SignupActivity, "Ya existe un usuario con este nombre.")
                            }
                        }
                        override fun onQueryFailure(exception: Exception) {
                            Log.e("User query failed", "${exception.message}")
                        }
                    })
                } else {
                    Notifications.shortToast(this, "Ya existe un usuario con este email.")
                }
            }.addOnFailureListener {
                Notifications.shortToast(this, "No se pudo crear el usuario.")
            }
        }
    }

    /** Método encargado de validar los campos introducidos para el registro del usuario.
     * @param username  Nombre de usuario con el que se reconocerá al usuario en la aplicación.
     * @param email     Email del usuario.
     * @param password  Contraseña del usuario.
     * @return True si es válido, False si es inválido.
     */
    private fun validateFields(username: String, email:String, password:String) : Boolean{
        if (!username.trim().matches(USERNAME_REGEX)) {
            Notifications.shortToast(this, "El nombre debe tener una longitud de entre 5 y 15 caracteres alfanuméricos.")
            return false
        }
        if (email.trim().isEmpty()){
            Notifications.shortToast(this, "El email está vacío.")
            return false
        }
        if (password.trim().length < 6) {
            Notifications.shortToast(this, "La contraseña debe tener un mínimo de 6 caracteres.")
            return false
        }
        if (txtConfirmPassword.text.trim().toString() != password){
            Notifications.shortToast(this, "Las contraseñas no coinciden.")
            return false
        }
        return true
    }
}