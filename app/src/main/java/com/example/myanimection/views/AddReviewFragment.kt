package com.example.myanimection.views

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myanimection.R
import com.example.myanimection.controllers.FirestoreQueryCallback
import com.example.myanimection.controllers.ReviewController
import com.example.myanimection.models.AnimeReview
import com.example.myanimection.utils.Notifications
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

/** Fragment donde se añaden o editan reseñas.
 */
class AddReviewFragment : Fragment() {

    private lateinit var txtTitle: TextView
    private lateinit var txtBody: TextView
    private lateinit var spinScore: Spinner
    private lateinit var txtTitleLimit: TextView
    private lateinit var txtBodyLimit: TextView
    private lateinit var btnSubmit: FloatingActionButton
    private val MIN_LENGTH = 5
    private val TITLE_LIMIT = 40
    private val BODY_LIMIT = 500
    private val reviewController = ReviewController()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var animeMediaId = arguments?.getInt("animeId", 0) ?: 0
        val reviewRawData = arguments?.getString("reviewData", "")
        val view = inflater.inflate(R.layout.fragment_add_review, container, false)
        txtTitle = view.findViewById(R.id.txtAddReviewTitle)
        txtBody = view.findViewById(R.id.txtAddReviewBody)
        txtTitleLimit = view.findViewById(R.id.txtTitleLimit)
        txtBodyLimit = view.findViewById(R.id.txtBodyLimit)
        spinScore = view.findViewById(R.id.spinAddReviewScore)
        btnSubmit = view.findViewById(R.id.btnSubmitReview)

        //  Limitación de caracteres.
        txtTitle.filters = arrayOf(InputFilter.LengthFilter(TITLE_LIMIT))
        txtBody.filters = arrayOf(InputFilter.LengthFilter(BODY_LIMIT))

        //  Contador de caracteres e indicadores para el usuario. Si no se cumple la longitud mínima, el texto será rojo.
        txtTitle.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val currentLength = s!!.trim().length
                txtTitleLimit.text = "$currentLength/$TITLE_LIMIT"
                if (currentLength < MIN_LENGTH) {
                    txtTitleLimit.setTextColor(ContextCompat.getColor(view.context, R.color.error))
                } else {
                    txtTitleLimit.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                }
            }
        })
        txtBody.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val currentLength = s!!.trim().length
                txtBodyLimit.text = "$currentLength/$BODY_LIMIT"
                if (currentLength < MIN_LENGTH) {
                    txtBodyLimit.setTextColor(ContextCompat.getColor(view.context, R.color.error))
                } else {
                    txtBodyLimit.setTextColor(ContextCompat.getColor(view.context, R.color.black))
                }
            }
        })

        val spinScoreAdapter = ArrayAdapter(view.context, R.layout.ani_spin_item, arrayOf(1,2,3,4,5,6,8,9,10))
        spinScoreAdapter.setDropDownViewResource(R.layout.ani_spin_dropdown_item)
        spinScore.adapter = spinScoreAdapter

        /* Para evitar mayores complicaciones haciendo que la review sea Serializable o Parcelable para transferirla de una vista a otra,
            he optado por convertirla a formato JSON para poder desconvertirla en la vista actual y solamente tener que pasar un
            String.*/
        if (reviewRawData != null && reviewRawData.isNotEmpty()) {
            val gson = Gson()
            val reviewData = gson.fromJson(reviewRawData, AnimeReview::class.java)
            animeMediaId = reviewData.animeId
            txtTitle.text = reviewData.title
            txtBody.text = reviewData.body
            spinScore.setSelection(reviewData.score-1)  //  Al ser una nota del 1 al 10 en orden, se escoge la posición restando 1 a la nota.
        }

        //  Si la ID del anime es 0 quiere decir que no se ha podido cargar, ya que los IDs empiezan desde 1 en adelante.
        //  Por tanto no se puede mandar la reseña y cancelo el evento.
        btnSubmit.setOnClickListener {
            if (animeMediaId == 0) {
                Notifications.alertDialogOK(view.context, "Error de carga","Hubo un problema al cargar el anime a reseñar.")
                return@setOnClickListener
            }
            val title = txtTitle.text.toString().trim()
            val body = txtBody.text.toString().trim()
            val score = spinScore.selectedItem as Int
            if  (body.length >= 5 && title.length >= 5) {
                if (Firebase.auth.currentUser != null) {
                    val uid = Firebase.auth.currentUser!!.uid
                    val review = AnimeReview(animeMediaId,uid, title, body, score, arrayListOf() )
                    reviewController.reviewExists(review, object: FirestoreQueryCallback {
                        override fun onQueryComplete(success: Boolean) {
                            //  Si la reseña existe, pregunta si se desea reemplazar.
                            if (success) {
                                Notifications.alertDialogOK(view.context, "Ya existe una reseña", "¿Deseas reemplezarla?",
                                    positiveButtonClickListener = { dialog ->
                                        reviewController.updateReview(review, object: FirestoreQueryCallback {
                                            override fun onQueryComplete(success: Boolean) {
                                                if (success) {
                                                    Notifications.shortToast(view.context, "Reseña actualizada.")
                                                    dialog.dismiss()
                                                } else {
                                                    Notifications.shortToast(view.context, "No se pudo actualizar la reseña.")
                                                }
                                            }
                                            override fun onQueryFailure(exception: Exception) {
                                                Notifications.shortToast(view.context, "Hubo un error al actualizar la reseña.")
                                                Log.e("EDIT REVIEW", exception.message.toString())
                                            }
                                        })
                                    },
                                    negativeButtonClickListener = { dialog ->
                                        dialog.dismiss()
                                    }
                                )
                                //  En caso contrario, se añadirá normalmente.
                            } else {
                                reviewController.addReview(review, object: FirestoreQueryCallback {
                                    override fun onQueryComplete(success: Boolean) {
                                        if (success) {
                                            Notifications.shortToast(view.context, "Reseña enviada con éxito.")
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        } else {
                                            Notifications.shortToast(view.context, "No se puedo enviar la reseña.")
                                        }
                                    }
                                    override fun onQueryFailure(exception: Exception) {
                                        Notifications.shortToast(view.context, "Error del servicio.")
                                        Log.e("AddReview", exception.message.toString())
                                    }
                                })
                            }
                        }
                        override fun onQueryFailure(exception: Exception) {
                            Notifications.shortToast(view.context, "No se ha podido comprobar la existencia de la reseña.")
                            Log.e("AddReview", exception.message.toString())
                        }
                    })
                }
            } else {
                Notifications.shortToast(view.context, "El título y cuerpo no pueden tener menos de 5 caracteres.")
            }
        }
        return view
    }

}