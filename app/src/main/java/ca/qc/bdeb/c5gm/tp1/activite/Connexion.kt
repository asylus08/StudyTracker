package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.dao.UtilisateurDao
import ca.qc.bdeb.c5gm.tp1.entite.Utilisateur
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class Connexion : AppCompatActivity() {

    // Éléments de l'interface
    private lateinit var saisiNomUtilisateur: EditText
    private lateinit var saisiMdp: EditText
    private lateinit var btnConnexion: Button
    private lateinit var lienVersInscription: TextView

    // Éléments de gestion de données
    private lateinit var session: GestionnaireSession
    private lateinit var firestore: FirebaseFirestore
    private lateinit var utilisateurDao: UtilisateurDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connexion)

        // Initialiser la base de donnée et la session
        firestore = Firebase.firestore
        utilisateurDao = UtilisateurDao.getInstance(firestore)
        session = GestionnaireSession.getInstance(this)

        initialiserElementInterface()
    }

    // Méthode pour initialiser les éléments de l'interface
    private fun initialiserElementInterface(){
        saisiNomUtilisateur = findViewById(R.id.saisiNomUtilisateur)
        saisiMdp = findViewById(R.id.saisiMdp)
        btnConnexion = findViewById(R.id.btnConnexion)
        lienVersInscription = findViewById(R.id.versIncription)

        lienVersInscription.setOnClickListener { naviguerVersAutrePage(Inscription::class.java) }
        btnConnexion.setOnClickListener { connecterUtilisateur() }
    }

    // Méthode pour connecter un utilisateur
    private fun connecterUtilisateur() {
        // On prend les valeurs des EditText
        val nomUtilisateur = saisiNomUtilisateur.text.toString()
        val mdp = saisiMdp.text.toString()

        // On verifie s'il un champ est vide
        if (nomUtilisateur.isEmpty() || mdp.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_champs_manquant), Snackbar.LENGTH_LONG).apply {
                setBackgroundTint(ContextCompat.getColor(this@Connexion, R.color.blanc))
                setTextColor(ContextCompat.getColor(this@Connexion, R.color.noir))
                show()
            }
            return
        }

        // Coroutine pour l'opération de base de donnée
        lifecycleScope.launch(Dispatchers.IO) {
            // Bloc try en cas d'erreur
            try {
                // On vérifie l'existence du compte
                val utilisateur = utilisateurDao.getUtilisateurParInfo(nomUtilisateur, mdp)

                withContext(Dispatchers.Main) {
                    // S'il existe, on connect l'utilisateur
                    if (utilisateur != null) {
                        val streakVerifier = verifierStreak(utilisateur)
                        // On garde dans le singleton, son id pour les opértions futurs et son thème
                        session.connexionUtilisateur(utilisateur.idUtilisateurPK, utilisateur.modeSombre, streakVerifier, utilisateur.dernierDateStreak)
                        naviguerVersAutrePage(HistoriqueEtude::class.java) // On lui redirige vers la première page
                        finish() // En théorie, une fois que l'utilisateur connect, on n'a pas besoin de cette activité
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_connexion), Snackbar.LENGTH_LONG).apply {
                            setBackgroundTint(ContextCompat.getColor(this@Connexion, R.color.blanc))
                            setTextColor(ContextCompat.getColor(this@Connexion, R.color.noir))
                            show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Connexion, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Connexion, R.color.noir))
                        show()
                    }
                    Log.d("test", e.toString())
                }
            }
        }
    }

    // Méthode pour vérifier le streak de l'utilisateur
    private fun verifierStreak(utilisateur: Utilisateur): Int{
        val dateCourant = LocalDate.now() // On récupère la date courant

        // On assure que le streak n'est pas null
        if (!utilisateur.dernierDateStreak.isNullOrEmpty()) {
            // On convertie la date sauvegarder en object LocalDate
            val dernierDate = LocalDate.parse(
                utilisateur.dernierDateStreak,
                DateTimeFormatter.ISO_LOCAL_DATE
            )

            // Vérifie la différence de jours entre les 2 dates
            val differenceJour = ChronoUnit.DAYS.between(dernierDate, dateCourant)

            // Si la différence est plus qu'une journée, on réinitialise le streak à 0
            if (differenceJour > 1) {
                utilisateur.streak = 0
                lifecycleScope.launch(Dispatchers.IO) {
                    utilisateurDao.updateUtilisateur(utilisateur)
                }
            }
        }
        return utilisateur.streak
    }

    // Méthode pour naviguer dans un autre activité
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(this, page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }
}