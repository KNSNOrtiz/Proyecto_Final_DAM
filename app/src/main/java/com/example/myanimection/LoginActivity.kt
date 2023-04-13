package com.example.myanimection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val btnSignIn : Button = findViewById(R.id.btnSign)
        val txtEmailSign : EditText = findViewById(R.id.txtEmailSign)
        val txtPasswordSign : EditText = findViewById(R.id.txtPasswordSign)


        btnSignIn.setOnClickListener { signIn(txtEmailSign.text.toString(), txtPasswordSign.text.toString()) }
    }

    private fun validateFields(email:String, password:String) : Boolean{
        return (email.isEmpty() || password.length < 6)
    }

    private fun signIn(email:String, password:String){
        if (validateFields(email,password)){
            Toast.makeText(this,"El campo email y contraseña no son válidos.", Toast.LENGTH_SHORT).show()
        } else{
            //Toast.makeText(this, "Campos rellenos.", Toast.LENGTH_SHORT).show()
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                try{
                    it.result.user?.sendEmailVerification()
                    Toast.makeText(this,"Usuario creado con éxito. Verifica tu email.", Toast.LENGTH_SHORT).show()
                } catch (ex : FirebaseAuthWeakPasswordException){
                    Toast.makeText(this,"La contraseña debe contener un mínimo de 6 caracteres.", Toast.LENGTH_SHORT).show()
                }


            }.addOnFailureListener {
                Toast.makeText(this,"No se pudo dar de alta el usuario.", Toast.LENGTH_SHORT).show()
            }

        }
    }
}