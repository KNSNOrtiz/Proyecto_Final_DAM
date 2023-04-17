package com.example.myanimection.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.myanimection.R
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC
}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnLogIn : Button = findViewById(R.id.btnLogin)
        val txtSignUp : TextView = findViewById(R.id.txtSignUp)
        val txtEmailSign : EditText = findViewById(R.id.txtEmailSignin)
        val txtPasswordSign : EditText = findViewById(R.id.txtPasswordSignin)
        supportActionBar?.hide()

        txtSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        btnLogIn.setOnClickListener { signIn(txtEmailSign.text.toString(), txtPasswordSign.text.toString()) }
    }


    //  INICIAR SESIÓN CON EMAIL/PASSWORD.
    private fun signIn(email:String, password: String){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                var intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            } else{
                Notifications.shortToast(this, "El usuario especificado no existe.")
            }
        }
    }
}