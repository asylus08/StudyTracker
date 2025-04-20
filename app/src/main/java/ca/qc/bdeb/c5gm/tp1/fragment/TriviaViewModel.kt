package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qc.bdeb.c5gm.tp1.API.API
import ca.qc.bdeb.c5gm.tp1.API.Question
import ca.qc.bdeb.c5gm.tp1.BuildConfig
import ca.qc.bdeb.c5gm.tp1.dao.UtilisateurDao
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.view.View
import androidx.core.content.ContextCompat
import ca.qc.bdeb.c5gm.tp1.R
import com.google.android.material.snackbar.Snackbar

class TriviaViewModel: ViewModel() {
    val firestore = Firebase.firestore
    val utilisateurDao = UtilisateurDao.getInstance(firestore)
    val question = MutableLiveData<Question>()

    // Méthode pour faire une demande API pour une question trivia
    fun genererQuestion(category: String, view: View) {
        viewModelScope.launch {
            val reponse = withContext(Dispatchers.IO) {
                API.apiService.getUneQuestion(BuildConfig.API_KEY, category)
            }

            if (reponse == null) {
                // Erreur de la recherche internet
                Snackbar.make(view, R.string.erreur_recherche_internet, Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(view.context, R.color.blanc))
                    setTextColor(ContextCompat.getColor(view.context, R.color.noir))
                }.show()
                return@launch
            }

            if (!reponse.isSuccessful || reponse.body().isNullOrEmpty()) {
                // Erreur API
                Snackbar.make(view, R.string.erreur_aucune_question, Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(view.context, R.color.blanc))
                    setTextColor(ContextCompat.getColor(view.context, R.color.noir))
                    setAction(R.string.reessayer) {
                        genererQuestion(category, view) // Relance l'appel API
                    }
                }.show()
                return@launch
            }
            question.value = reponse.body()!![0]

            // Pour faciliter la correction et test de cette fonction
            Log.d("QUESTION", question.value!!.question)
            Log.d("REPONSE", question.value!!.answer)
        }
    }

    // Méthode pour valider la réponse utilisateur
    fun validerQuestion(reponseUtilisateur: String, session: GestionnaireSession): Boolean {
        // On récupère la bonne reponse
        val bonneReponse = question.value!!.answer.toString()
        // On le compare
        if (bonneReponse.equals(reponseUtilisateur.trim(), ignoreCase = true)) {
            // Si il est valide
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    // On incrémente le streak comme point bonus
                    var utilisateur = utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())
                    utilisateur = utilisateur!!.copy(streak = utilisateur.streak + 1)

                    utilisateurDao.updateUtilisateur(utilisateur)
                    session.continueStreakCourant()
                }
            }
            return true
        }
        return false
    }
}