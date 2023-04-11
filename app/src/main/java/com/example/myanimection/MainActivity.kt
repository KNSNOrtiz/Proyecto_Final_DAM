package com.example.myanimection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var txtPrueba : TextView = findViewById(R.id.txtPrueba)
        val btnLeer : Button = findViewById(R.id.btnLeer)
        val btnEscribir : Button = findViewById(R.id.btnEscribir)


        //  FIREBASE
        val db = Firebase.firestore   //  Referencia a la DB.
        val refTest = db.collection("test").get()   //  Referencia a la colección.
        val refDoc = db.collection("test").document("hola-mundo")

        //  Acciones dependiendo de si se conecta o no.
        refTest.addOnSuccessListener {
            Toast.makeText(this, "Conectado a la DB", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al conectar a la BD",Toast.LENGTH_SHORT).show()
        }

        btnLeer.setOnClickListener {
           refTest.addOnSuccessListener { result ->
               for (document in result){
                   Log.d("DOCUMENTO", "Documento encontrado")
                   txtPrueba.setText(document.data.getValue("saludo").toString() + "\n" + document.data.getValue("despedida").toString())
               }
            }
            refTest.addOnFailureListener {
                Log.d("ERROR DE LECTURA", "No se han podido leer los documentos.")
            }
        }

        btnEscribir.setOnClickListener {
            //  Datos que se van a insertar en el documento "hola-mundo" de la colección.
            refDoc.update("despedida", "Adios mundo cruel").addOnFailureListener {
                Toast.makeText(this, "No se ha podido editar el campo", Toast.LENGTH_SHORT)
            }.addOnSuccessListener {
                Toast.makeText(this, "Se ha añadido el campo!", Toast.LENGTH_SHORT)
            }
        }



    }
}