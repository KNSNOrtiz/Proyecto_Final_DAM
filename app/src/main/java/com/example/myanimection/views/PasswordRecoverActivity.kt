package com.example.myanimection.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.myanimection.R
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.FirebaseAuth

/** Activity encargado de permitir a los usuarios recuperar su contraseña mediante email.*/
class PasswordRecoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recover)
        supportActionBar?.hide()

        val txtEmail: TextView = findViewById(R.id.txtEmailRecover)
        val btnSendEmail: Button = findViewById(R.id.btnSendRecoverEmail)

        //  Firebase Authentication envía un email de recuperación de contraseña al usuario identificado por el email especificado.
        btnSendEmail.setOnClickListener {
            val email = txtEmail.text.toString()
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            Notifications.alertDialogOK(this, "Recuperación de contraseña", "Se ha enviado un enlace al email para recuperar la cuenta." ,
            positiveButtonClickListener = {onBackPressedDispatcher.onBackPressed()},
            negativeButtonClickListener = null)
        }

    }
}