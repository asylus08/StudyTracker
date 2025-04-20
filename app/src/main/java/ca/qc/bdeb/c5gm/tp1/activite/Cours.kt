package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeCoursAdaptor
import ca.qc.bdeb.c5gm.tp1.dao.LesCoursDao
import ca.qc.bdeb.c5gm.tp1.entite.LesCours
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Cours : AppCompatActivity() {
    // Élement de gestion
    private lateinit var firestore: FirebaseFirestore
    private lateinit var lesCoursDao: LesCoursDao
    private lateinit var session: GestionnaireSession

    // Liste pour afficher les cours
    private var listeCours: MutableList<LesCours> = mutableListOf()

    // Recycler view
    private lateinit var recyclerCours: RecyclerView

    // Adapter
    private lateinit var adapter: ListeCoursAdaptor

    // Élement interface
    private lateinit var btnAjouterCours: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        // On initialise avant le onCreate, pour pouvoir appliquer le thème correctement
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.cours)

        firestore = Firebase.firestore
        lesCoursDao = LesCoursDao.getInstance(firestore)

        initialiserElementInterface()
    }

    override fun onStart() {
        super.onStart()
        // On initialise les adapters et on charge les cours quand l'activité revient en avant
        chargerLesCours()
        initialiserAdapter()
        Log.d("COURS", listeCours.toString())
    }

    // Méthode pour initialiser les éléments de l'interface
    private fun initialiserElementInterface(){
        recyclerCours = findViewById(R.id.RecyclerCours)

        btnAjouterCours = findViewById(R.id.btnAjouterCours)
        btnAjouterCours.setOnClickListener{ montrerDialogueAjoutCours() }

    }

    // Méthode pour initialiser l'adapter
    private fun initialiserAdapter() {
        recyclerCours.layoutManager = LinearLayoutManager(this) // Liste vertical

        // On lui passe la liste et la fonction nécessaire
        adapter = ListeCoursAdaptor(listeCours,
            { cours -> supprimerCours(cours) })

        recyclerCours.adapter = adapter
    }

    // Méthode pour créer la dialogue pour ajouter un cours
    // Source: https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
    private fun montrerDialogueAjoutCours() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.message_ajout_cours)) // On lui donne un titre

        val champSaisiCours = EditText(this) // On lui donne un EditText pour écrire un nom de cours
        builder.setView(champSaisiCours) // On l'intègre dans l'interface

        // Bouton pour le cas positive
        builder.setPositiveButton(getString(R.string.ajouter)) { dialog, which ->
            val nomCours = champSaisiCours.text.toString()
            // Assurer que le nom de cours n'est pas vide
            if (nomCours.isNotBlank()) {
                ajouterCours(nomCours) // Appelle la méthode pour ajouter un cours
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_titre_cours_invalide), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Cours, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Cours, R.color.noir))
                    show()
                }
            }
        }
        // Pour le cas négative, on annule simplement la dialogue
        builder.setNegativeButton(getString(R.string.annuler)) { dialog, which -> dialog.cancel() }
        builder.show()
    }

    // Méthode pour ajouter un cours
    private fun ajouterCours(nomCours: String) {
        // On crée un instance de cours
        val nouvelleCours = LesCours(
            idUtilisateurFK = session.retournerIdUtilisateur(),
            nomCours = nomCours
        )
        // On crée une coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                lesCoursDao.insertCours(nouvelleCours) // On insert le cours

                // On utilise la coroutine main pour ce qui est de l'interface
                withContext(Dispatchers.Main) {
                    listeCours.add(nouvelleCours) // On ajoute le cours dans le recycler view
                    adapter.mettreAJourDonnee(listeCours) // On notifie le changement
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Cours, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Cours, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour charger les cours quand l'activité vient en avant
    private fun chargerLesCours() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On cherche les cours dans la base de donnée selon l'id de l'utilisateur
                listeCours = lesCoursDao.getCoursParIdUtilisateur(session.retournerIdUtilisateur())

                withContext(Dispatchers.Main) {
                    adapter.mettreAJourDonnee(listeCours) // On notifie les changements de la liste
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Cours, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Cours, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour supprimer un cours
    private fun supprimerCours(cours: LesCours) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                lesCoursDao.deleteCours(cours) // On appelle la base de données pour supprimer le cours

                withContext(Dispatchers.Main) {
                    listeCours.remove(cours) // On l'enleve de la recycler view
                    adapter.mettreAJourDonnee(listeCours) // On notifie le changement
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Cours, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Cours, R.color.noir))
                        show()
                    }
                }
            }
        }
    }
}