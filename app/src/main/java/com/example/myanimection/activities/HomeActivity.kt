package com.example.myanimection.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.myanimection.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var txtWelcome : TextView = findViewById(R.id.txtWelcome)
        val btnRead : Button = findViewById(R.id.btnRead)
        val btnUpdate : Button = findViewById(R.id.btnUpdate)


        //  FIREBASE
        val user = FirebaseAuth.getInstance().currentUser
        val db = Firebase.firestore   //  Referencia a la DB.
        val refTest = db.collection("test").get()   //  Referencia a la colección.
        val refDoc = db.collection("test").document("hola-mundo")

        //  Acciones dependiendo de si se conecta o no.
        txtWelcome.text = "¡Bienvenido/a, ${user?.email}"


        refTest.addOnSuccessListener {
            Toast.makeText(this, "Conectado a la DB", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al conectar a la BD",Toast.LENGTH_SHORT).show()
        }

        btnRead.setOnClickListener {
           refTest.addOnSuccessListener { result ->
               for (document in result){
                   Log.d("DOCUMENTO", "Documento encontrado")
                   txtWelcome.setText(document.data.getValue("saludo").toString() + "\n" + document.data.getValue("despedida").toString())
               }
            }
            refTest.addOnFailureListener {
                Log.d("ERROR DE LECTURA", "No se han podido leer los documentos.")
            }
        }

        btnUpdate.setOnClickListener {
            //  Datos que se van a insertar en el documento "hola-mundo" de la colección.
            refDoc.update("despedida", "Adios mundo cruel").addOnFailureListener {
                Toast.makeText(this, "No se ha podido editar el campo", Toast.LENGTH_SHORT)
            }.addOnSuccessListener {
                Toast.makeText(this, "Se ha añadido el campo!", Toast.LENGTH_SHORT)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_action_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.itemLogOut -> {
               if (FirebaseAuth.getInstance() != null){
                   FirebaseAuth.getInstance().signOut()
                   onBackPressedDispatcher.onBackPressed()
               }
                true
            } else -> super.onOptionsItemSelected(item)

        }
    }
}