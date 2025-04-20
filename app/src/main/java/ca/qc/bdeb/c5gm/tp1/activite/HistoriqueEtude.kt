package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.TacheNotification
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeSessionAdaptor
import ca.qc.bdeb.c5gm.tp1.dao.EtudeDao
import ca.qc.bdeb.c5gm.tp1.entite.Etude
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HistoriqueEtude : AppCompatActivity() {

    // Élements interfaces
    private lateinit var btnDemarrerSessionEtude: Button

    // Les recycler views
    private lateinit var recyclerHistoriqueEtude: RecyclerView

    // Adaptor
    private lateinit var itemSessionAdaptor: ListeSessionAdaptor

    // Base de données
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etudeDao: EtudeDao

    // Gestionnaire de session
    private lateinit var session: GestionnaireSession

    // Liste de session etude
    private var listeEtude: MutableList<Etude> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        // On applique le thème selon la préférence utilisateur
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.historique_etude)

        // Initialisation de la base de donnée
        firestore = Firebase.firestore
        etudeDao = EtudeDao.getInstance(firestore)

        initialiserElementInterface()

        // On initialise le travailleur de notification
        val travailNotification = OneTimeWorkRequestBuilder<TacheNotification>()
            .setInitialDelay(1 , TimeUnit.HOURS)
            .build()

        // On lui donne le travail
        WorkManager.getInstance(applicationContext).enqueue(travailNotification)

        // source: https://stackoverflow.com/questions/44305206/ask-permission-for-push-notification
        // On fait une demande de permission
        val dialogueDemandePermissionNotif = registerForActivityResult(ActivityResultContracts.RequestPermission()){ }
        dialogueDemandePermissionNotif.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onStart() {
        super.onStart()
        // Charge les sessions d'étude et initialise l'adapter quand l'activité revient en avant plan
        initialiserAdaptor()
        chargerSessionEtude()
    }

    // Méthode pour initialiser les éléments de l'interface
    private fun initialiserElementInterface(){
        recyclerHistoriqueEtude = findViewById(R.id.recyclerHistoriqueSession)

        btnDemarrerSessionEtude = findViewById(R.id.btnDemarrerSession)
        btnDemarrerSessionEtude.setOnClickListener{ naviguerVersAutrePage(SessionEtude::class.java) }
    }

    // Méthode pour initialiser l'adapter
    private fun initialiserAdaptor(){
        // Logique d'affaire similaire à Cours
        recyclerHistoriqueEtude.layoutManager = LinearLayoutManager(this)

        itemSessionAdaptor = ListeSessionAdaptor(
            listeEtude,
            { etude -> supprimerSessionEtude(etude) }
        )
        recyclerHistoriqueEtude.adapter = itemSessionAdaptor
    }

    // Méthode pour charger les session d'étude
    private fun chargerSessionEtude() {
        // Logique d'affaires similaire à Cours
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                listeEtude = etudeDao.getEtudesParIdUtilisateuer(session.retournerIdUtilisateur())

                withContext(Dispatchers.Main) {
                    itemSessionAdaptor.mettreAJourDonnee(listeEtude)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@HistoriqueEtude, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@HistoriqueEtude, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour supprimer une session d'étude
    private fun supprimerSessionEtude(etude: Etude) {
        // Logique d'affaires similaires à Cours
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                etudeDao.deleteEtude(etude)

                withContext(Dispatchers.Main) {
                    listeEtude.remove(etude)
                    itemSessionAdaptor.mettreAJourDonnee(listeEtude)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@HistoriqueEtude, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@HistoriqueEtude, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour naviguer à une autre page
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(this, page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }
}