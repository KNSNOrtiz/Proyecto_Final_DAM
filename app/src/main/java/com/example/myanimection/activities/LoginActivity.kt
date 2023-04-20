package com.example.myanimection.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.example.myanimection.R
import com.example.myanimection.utils.Notifications
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.w3c.dom.Text

enum class ProviderType{
    BASIC
}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var isPassVisible = false

        val btnLogIn : Button = findViewById(R.id.btnLogin)
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
        btnLogIn.setOnClickListener { signIn(txtEmailSign.text.toString(), txtPasswordSign.text.toString()) }
        
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
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null){
                        if (user.isEmailVerified){
                            var intent = Intent(this, HomeActivity::class.java)
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

    private fun validateFields(email:String, password:String) : Boolean{
        if (email.isEmpty() || password.isEmpty()){
            Notifications.shortToast(this, "Los campos están vacíos.")
            return false
        }
        return true
    }


}