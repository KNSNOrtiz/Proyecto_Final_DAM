package com.example.myanimection.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.example.myanimection.R
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.models.UserLists
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    lateinit var txtConfirmPassword : EditText
    private var userController = UserController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        val btnSignup: Button = findViewById(R.id.btnSignup)
        val btnViewPassword: ImageButton = findViewById(R.id.btnViewPasswordSignUp)
        val txtEmail : TextView = findViewById(R.id.txtEmailSignUp)
        val txtPassword : EditText = findViewById(R.id.txtPasswordSignUp)
        txtConfirmPassword = findViewById(R.id.txtConfirmPasswordSignUp)


        var isPassVisible = false

        btnSignup.setOnClickListener {
            var email = txtEmail.text.toString()
            var password = txtPassword.text.toString()
            signUp(email, password) }

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
    //  MÉTODO ENCARGADO DE CREAR USUARIOS CON EMAIL Y CONTRASEÑA.
    private fun signUp(email:String, password:String){
        if (validateFields(email,password)){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    val user = it.result.user
                    Log.d("CREACIÓN CUENTA", user?.metadata?.creationTimestamp.toString() )
                    Log.d("ÚLTIMA SESIÓN CUENTA", user?.metadata?.lastSignInTimestamp.toString() )
                    val additionalInfo = it.result.additionalUserInfo
                    if (additionalInfo != null) {
                        if (additionalInfo.isNewUser) {
                            userController.isUserRegistered(user!!.uid, object: UserController.FirestoreQueryCallback {
                                override fun onQueryComplete(success: Boolean) {
                                    if (!success) {
                                        //  Si el displayName es nulo ya que se hace desde un correo electrónico, se genera uno hasta la @  a partir del email dado.
                                        val displayName = user.displayName ?: user.email!!.substring(0, user.email!!.indexOf('@'))
                                        userController.addUser(
                                            User(user.uid, displayName, user.email!!, UserLists(
                                            arrayListOf(), arrayListOf(), arrayListOf(), arrayListOf()
                                        )
                                            )
                                        )
                                    }
                                }
                                override fun onQueryFailure(exception: Exception) {
                                    Log.e("User query failed", "${exception.message}")
                                }
                            })
                        }
                    }
                    user?.sendEmailVerification()
                    Notifications.alertDialogOK(this, "Usuario creado", "Se ha enviado " +
                            "un enlace de verificación a tu email. Verífica tu usuario para completar el registro.")
                } else{
                    Notifications.shortToast(this, "Ya existe un usuario con este email.")
                }
            }.addOnFailureListener {
                Notifications.shortToast(this, "No se pudo crear el usuario.")
            }
        }
    }

    private fun validateFields(email:String, password:String) : Boolean{
        if (email.isEmpty() || password.length < 6){
            Notifications.shortToast(this, "El email está vacío o la contraseña no tiene la longitud mínima.")
            return false
        }
        if (txtConfirmPassword.text.toString() != password){
            Notifications.shortToast(this, "Las contraseñas no coinciden.")
            return false
        }
        return true
    }
}