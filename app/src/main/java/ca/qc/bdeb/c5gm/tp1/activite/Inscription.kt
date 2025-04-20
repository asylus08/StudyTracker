package ca.qc.bdeb.c5gm.tp1.activite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

class Inscription : AppCompatActivity() {
    // Éléments d'interface
    private lateinit var saisiNomUtilisateur: EditText
    private lateinit var saisiMdp: EditText
    private lateinit var saisiConfimerMdp: EditText
    private lateinit var btnInscription: Button
    private lateinit var lienVersConnexion: TextView

    // Base de donnée
    private lateinit var firestore: FirebaseFirestore
    private lateinit var utilisateurDao: UtilisateurDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inscription)

        // Initialise la base de données
        firestore = Firebase.firestore
        utilisateurDao = UtilisateurDao.getInstance(firestore)

        initialiserElementInterface()
    }

    // Méthode pour initialiser les éléments d'interface
    private fun initialiserElementInterface() {
        saisiNomUtilisateur = findViewById(R.id.saisiNomInscription)
        saisiMdp = findViewById(R.id.saisiMdpInscription)
        saisiConfimerMdp = findViewById(R.id.confirmationMdpInscription)
        btnInscription = findViewById(R.id.btnInscription)
        lienVersConnexion = findViewById(R.id.versConnexion)

        lienVersConnexion.setOnClickListener{
          naviguerVersAutrePage(Connexion::class.java)
        }

        btnInscription.setOnClickListener{
            verifierInfoInscription()
        }
    }

    // Méthode pour vérifier les champs de saisi d'inscription
    private fun verifierInfoInscription() {
        val nomUtilisateur = saisiNomUtilisateur.text.toString()
        val motDePasse = saisiMdp.text.toString()
        val confirmationMDP = saisiConfimerMdp.text.toString()

        when {
            nomUtilisateur.isEmpty() || motDePasse.isEmpty() || confirmationMDP.isEmpty() -> {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_champs_manquant), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Inscription, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Inscription, R.color.noir))
                    show()
                }
            }
            motDePasse != confirmationMDP -> {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_mdp_different), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Inscription, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Inscription, R.color.noir))
                    show()
                }
            }
            else -> {
                // Quand les champs de saisi sont remplis et les mots de passes se correspond, on vérifie si l'utilisateur existe déjà
                verifierExistenceUtilisateur(nomUtilisateur, motDePasse)
            }
        }
    }

    // Méthode pour vérifier si l'utilisateur existe déjà ou non
    @SuppressLint("ShowToast")
    private fun verifierExistenceUtilisateur(nomUtilisateur: String, motDePasse: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On cherche l'utilisateur dans la base de donnée
                val utilisateur = utilisateurDao.getUtilisateurParNomUtilisateur(nomUtilisateur)

                withContext(Dispatchers.Main) {
                    // Si l'utilisateur n'existe pas, on crée un nouvel utilisateur
                    if (utilisateur == null) {
                        creerNouvelUtilisateur(nomUtilisateur, motDePasse)
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_nom_existant), Snackbar.LENGTH_LONG).apply {
                            setBackgroundTint(ContextCompat.getColor(this@Inscription, R.color.blanc))
                            setTextColor(ContextCompat.getColor(this@Inscription, R.color.noir))
                            show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Inscription, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Inscription, R.color.noir))
                        show()
                    }
                }
            }
        }
    }


    // Méthode pour créer un nouvel utilisateur
    private fun creerNouvelUtilisateur(nomUtilisateur: String, motDePasse: String) {
        // Information de l'utilisateur à créer
        // Initialise la date du "premier streak" de l'utilisateur
        val dateInit = LocalDate.now().minusDays(1)
        val dateFomatter = dateInit.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val nouvelleUtilisateur = Utilisateur(
            nomUtilisateur = nomUtilisateur,
            motDePasse = motDePasse,
            dernierDateStreak = dateFomatter
        )

        lifecycleScope.launch(Dispatchers.IO){
            try {
                // On crée l'utilisateur
                utilisateurDao.insertUtilisateur(nouvelleUtilisateur)
            } catch (erreur: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_inscription), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Inscription, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Inscription, R.color.noir))
                        show()
                    }
                }
            }
        }
        // On navigue à la page de connexion
        naviguerVersAutrePage(Connexion::class.java)
    }

    // Méthode pour naviguer à une autre activité
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(this, page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }
}