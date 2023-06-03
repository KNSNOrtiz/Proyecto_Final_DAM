package com.example.myanimection.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.UserController
import com.example.myanimection.models.User
import com.example.myanimection.utils.Notifications
import com.example.myanimection.utils.Utilities
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

/** Activity encargado de representar la pantalla de inicio de sesión con email/contraseña y Google.
 */
class LoginActivity : AppCompatActivity() {

    //  Instancia de la clase controladora para gestionar usuarios desde Firebase.
    private val userController = UserController()
    private lateinit var mainActivityIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mainActivityIntent = Intent(this, MainActivity::class.java)

        /*  En las versiones más recientes se debe usar un launcher para lanzar activities con un resultado.
            En este caso, para obtener la pantalla de inicio de sesión de Google y obtener la cuenta elegida,
            se extrae los datos del resultado y las credenciales para darlas de alta en Firebase Authentication.

            Esto creará un nuevo usuario o vinculará su cuenta a Google de haberse registrado con email y contraseña.
         */
        val resultGoogleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)    //  Obtención de la credencial con el token de la cuenta.
                        FirebaseAuth.getInstance().signInWithCredential(credential)     //  Inicio de sesión con la credencial de Google.
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    //  Usuario que ha iniciado sesión. En este caso, el de Google.
                                    val user = FirebaseAuth.getInstance().currentUser
                                    //  Comprobamos si es el primer inicio de sesión y si no está registrado en la colección Users para darlo de alta en la BD.
                                    //  Se hace aquí para evitar hacer lecturas continuamente.
                                    val additionalInfo = it.result.additionalUserInfo
                                    //  REGISTRO
                                    if (additionalInfo != null) {
                                        if (additionalInfo.isNewUser) {
                                            userController.isUserRegistered(user!!.uid, object:FirestoreQueryCallback {
                                                override fun onQueryComplete(success: Boolean) {
                                                    if (!success) {
                                                        userController.addUser(User(user.uid, "", user.email!!, arrayListOf(), ""), object: FirestoreQueryCallback{
                                                            override fun onQueryComplete(success: Boolean) {
                                                                Notifications.shortToast(this@LoginActivity, "Registro completado.")
                                                                // Asignación del nombre de usuario obligatoria.
                                                                checkUsername(user.uid)
                                                            }
                                                            override fun onQueryFailure(exception: Exception) {
                                                                Notifications.shortToast(this@LoginActivity, "Hubo un error en el registro.")
                                                                Log.e("LOGIN", exception.message.toString())
                                                            }
                                                        })
                                                    }
                                                }
                                                override fun onQueryFailure(exception: Exception) {
                                                    Log.e("User query failed", "${exception.message}")
                                                }
                                            })
                                        }
                                    }
                                    //  INICIO DE SESIÓN.
                                    //  Comprobación del nombre de usuario en el login hasta que este no esté asignado correctamente.
                                    //  Si no, no podrá iniciar sesión.
                                    userController.getUserName(user!!.uid, object: UserController.StringQueryCallback {
                                        override fun onQueryComplete(result: String) {
                                            if (result.isNotEmpty()) {
                                                startActivity(mainActivityIntent)
                                            } else {
                                                checkUsername(user.uid)
                                            }
                                        }
                                        override fun onQueryFailure(exception: Exception) {
                                            Notifications.shortToast(this@LoginActivity, "Error en el inicio de sesión.")
                                            Log.e("LOGIN GOOGLE", exception.message.toString() )
                                        }
                                    })
                                } else {
                                    Notifications.shortToast(
                                        this,
                                        "No se pudo iniciar sesión con Google."
                                    )
                                }
                            }.addOnFailureListener {
                                Notifications.shortToast(
                                    this,
                                    "No se pudo iniciar sesión con Google."
                                )
                            }
                    }
                }
            }

        var isPassVisible = false
        val btnLogIn : Button = findViewById(R.id.btnLogin)
        val btnGoogleLogIn: Button = findViewById(R.id.btnGoogleLogIn)
        val txtSignUp : TextView = findViewById(R.id.txtSignUp)
        val txtEmailSign : EditText = findViewById(R.id.txtEmailSignin)
        val txtPasswordSign : EditText = findViewById(R.id.txtPasswordSignin)
        val txtForgotPassword: TextView = findViewById(R.id.txtForgotPassword)
        val btnViewPassword : ImageButton = findViewById(R.id.btnViewPassword)

        //  Ocultar la barra de acción superior.
        supportActionBar?.hide()

        //  Botón que lleva al Activity de registro.
        txtSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        //  Iniciar sesión.
        btnLogIn.setOnClickListener { signIn(txtEmailSign.text.toString().trim(), txtPasswordSign.text.toString().trim()) }

        //  Iniciar sesión con Google.
        btnGoogleLogIn.setOnClickListener {
            googleSignIn(resultGoogleLauncher)
        }
        //  Botón que lleva a la pantalla de recuperación de contraseñas.
        txtForgotPassword.setOnClickListener {
            val intent = Intent(this, PasswordRecoverActivity::class.java)
            startActivity(intent)
        }

        //  Botón que muestra u oculta la contraseña cambiando la forma en la que se muestra el texto.
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
                    val user = it.result.user
                    if (user != null){
                        if (user.isEmailVerified) {
                            startActivity(mainActivityIntent)
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

    /** Método encargado del Login con Google realizando una invocación a un Intent de Google y seleccionando un email.
     * @param launcher Intent que gestiona el Login como tal y muestra un fragment con las cuentas de Google disponibles.
     */
    private fun googleSignIn(launcher: ActivityResultLauncher<Intent>) {
        val conf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()
        val client = GoogleSignIn.getClient(this, conf)
        client.signOut()
        launcher.launch(client.signInIntent)
    }

    /** Método que fuerza a los usuarios de Google a tener un nombre de usuario antes de poder iniciar sesión y acceder al resto de la aplicación.
      * @param uid  UID del usuario del que se obtendrá su nombre de usuario en Firestore.
      */
    @SuppressLint("CheckResult")
    private fun checkUsername(uid: String) {
        val context = this@LoginActivity
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        MaterialDialog(context).show {
            title(text = "Establecer nombre de usuario")
            input(hint = "Nombre de usuario (5-15 caracteres)") { dialog, text ->
                val userName = text.toString().trim()
                //  Validación del nombre de usuario introducido mediante expresión regular.
                if (userName.matches(Utilities.USERNAME_REGEX)) {
                    userController.isUsernameTaken(userName, object: FirestoreQueryCallback {
                        override fun onQueryComplete(success: Boolean) {
                            if (success) {
                                Notifications.shortToast(context, "Nombre de usuario no disponible.")
                            } else {
                                userController.setUsername(uid, userName, object: FirestoreQueryCallback {
                                    override fun onQueryComplete(success: Boolean) {
                                        if (success) {
                                            dialog.dismiss()
                                            Notifications.shortToast(context, "Bienvenido/a, $userName." )
                                            startActivity(mainActivityIntent)
                                        } else {
                                            Notifications.shortToast(context, "Error al actualizar el usuario. Inténtalo de nuevo.")
                                        }
                                    }
                                    override fun onQueryFailure(exception: Exception) {
                                        Notifications.shortToast(context, "Error al actualizar el usuario. Inténtalo de nuevo.")
                                    }
                                })
                            }
                        }
                        override fun onQueryFailure(exception: Exception) {
                            Notifications.shortToast(context, "No se pudo recuperar el usuario.")
                        }
                    })
                } else {
                    Notifications.shortToast(context, "Solo se permiten 5-15 caracteres alfanuméricos sin espacios.")
                }
            }
            positiveButton(text = "Aceptar")
            negativeButton(text = "Cancelar", click = { dialog ->
                dialog.dismiss()
            })
        }
    }

    /** Función que comprueba superficialmente los datos introducidos en el login.
     * @param email     Email del usuario.
     * @param password  Contraseña del usuario.
     */
    private fun validateFields(email:String, password:String) : Boolean{
        if (email.isEmpty() || password.isEmpty()){
            Notifications.shortToast(this, "Los campos están vacíos.")
            return false
        }
        return true
    }


}