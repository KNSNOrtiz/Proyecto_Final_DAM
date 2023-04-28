package com.example.myanimection.utils

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//  CÓDIGO DE FIRESTORE DESPLAZADO A ESTA SECCIÓN TEMPORALMENTE. REFORMAS EN EL HOME.
fun main() {
    val db = Firebase.firestore   //  Referencia a la DB.
    val refTest = db.collection("test").get()   //  Referencia a la colección.
    val refDoc = db.collection("test").document("hola-mundo")

   /* refTest.addOnSuccessListener {
        Toast.makeText(this, "Conectado a la DB", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(this, "Error al conectar a la BD", Toast.LENGTH_SHORT).show()
    }

    btnRead.setOnClickListener {
        refTest.addOnSuccessListener { result ->
            for (document in result){
                Log.d("DOCUMENTO", "Documento encontrado")
                txtWelcome.text = document.data.getValue("saludo").toString() + "\n" + document.data.getValue("despedida").toString()
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
    }*/
}