package com.example.myanimection.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myanimection.R
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        val btnSignup: Button = findViewById(R.id.btnSignup)
        val txtEmail : TextView = findViewById(R.id.txtEmailSignUp)
        val txtPassword : TextView = findViewById(R.id.txtPasswordSignUp)




        btnSignup.setOnClickListener {
            var email = txtEmail.text.toString()
            var password = txtPassword.text.toString()
            signUp(email, password) }
        }
    //  MÉTODO ENCARGADO DE CREAR USUARIOS CON EMAIL Y CONTRASEÑA.
    private fun signUp(email:String, password:String){
        if (validateFields(email,password)){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful){
                    it.result.user?.sendEmailVerification()
                    MaterialDialog(this).show {
                        title(text="Usuario creado.")
                        message (text = "Se ha enviado un enlace de verificación a tu email. Verífica tu usuario para completar el registro.")
                    }
                } else{
                    Notifications.shortToast(this, "Ya existe un usuario con este email.")
                }
            }.addOnFailureListener {
                Notifications.shortToast(this, "No se pudo crear el usuario.")
            }
        }
    }

    private fun validateFields(email:String, password:String) : Boolean{
        val txtConfirmPassword : TextView = findViewById(R.id.txtConfirmPasswordSignUp)
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