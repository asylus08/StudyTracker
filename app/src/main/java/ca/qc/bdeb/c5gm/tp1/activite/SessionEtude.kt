package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.Taches
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeTacheAdaptor
import ca.qc.bdeb.c5gm.tp1.dao.EtudeDao
import ca.qc.bdeb.c5gm.tp1.dao.LesCoursDao
import ca.qc.bdeb.c5gm.tp1.dao.UtilisateurDao
import ca.qc.bdeb.c5gm.tp1.entite.Etude
import ca.qc.bdeb.c5gm.tp1.entite.LesCours
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SessionEtude : AppCompatActivity() {

    // Les champs textview
    private lateinit var txtNomSession: TextView
    private lateinit var txtChronometre: TextView

    // Les boutons présents dans l'activity
    private lateinit var btnControlTemps: ImageView
    private lateinit var btnAjoutTache: Button
    private lateinit var btnFinSession: Button

    // Les recycler view
    private lateinit var recyclerAFaire: RecyclerView
    private lateinit var recyclerFini: RecyclerView

    // Les listes à manipuler
    private var listeTacheAFaire: MutableList<Taches> = mutableListOf()
    private var listeTacheFini: MutableList<Taches> = mutableListOf()

    // Les adaptors
    private lateinit var adaptorAFaire: ListeTacheAdaptor
    private lateinit var adaptorFini: ListeTacheAdaptor

    private lateinit var chrono: CountDownTimer
    private lateinit var coursChoisi: String
    private var chronoEnFonction = false
    private var tempsEcouler = 0L
    private var tacheCompleter = 0
    private var tacheCreer = 0

    // Gestionnaire de session
    private lateinit var session: GestionnaireSession

    // Base de données
    private lateinit var firestore: FirebaseFirestore
    private lateinit var etudeDao: EtudeDao
    private lateinit var lesCoursDao: LesCoursDao
    private lateinit var utilisateurDao: UtilisateurDao

    // Lecteur de musique
    // Source: https://stackoverflow.com/questions/63398798/how-to-add-sound-effects-with-kotlin-in-android
    private var lecteurMusique: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Application d'un thème selon les préférences utilisateurs
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_etude)

        // Initialisation de la base de donnée
        firestore = Firebase.firestore
        lesCoursDao = LesCoursDao.getInstance(firestore)
        etudeDao = EtudeDao.getInstance(firestore)
        utilisateurDao = UtilisateurDao.getInstance(firestore)

        initialiserElementInterface()
    }

    override fun onStart() {
        super.onStart()
        // Re-initialiser certains éléments de l'interface
        listeTacheAFaire = mutableListOf()
        listeTacheFini = mutableListOf()
        chronoEnFonction = false
        coursChoisi = getString(R.string.aucun_valeur)
        tempsEcouler = 0L
        txtNomSession.text = ""
        txtChronometre.text = getString(R.string.valeur_chrono_defaut)
        btnControlTemps.setImageResource(R.drawable.baseline_play_arrow_24)
        tacheCompleter = 0
        tacheCreer = 0

        initialiserAdapters()
        montrerDialogueDebutSession()
        jouerMusique() // On joue de la musique
    }

    // Méthode pour jouer de la musique
    private fun jouerMusique(){
        lecteurMusique = MediaPlayer.create(this, R.raw.magnetic)
        lecteurMusique?.isLooping = true // On joue sans arrêt dans l'activité
        lecteurMusique?.start()
    }

    // Méthode pour intialiser les éléments de l'interface
    private fun initialiserElementInterface() {
        txtNomSession = findViewById(R.id.nomSessionEtude)
        txtChronometre = findViewById(R.id.chronometre)

        btnControlTemps = findViewById(R.id.btnControlTemps)
        btnFinSession = findViewById(R.id.btnFinSession)
        btnAjoutTache = findViewById(R.id.btnAjoutTache)

        recyclerAFaire = findViewById(R.id.recyclerViewAFaire)
        recyclerFini = findViewById(R.id.recyclerViewFini)

        btnAjoutTache.setOnClickListener { activerDialogAjoutTache() }
        btnControlTemps.setOnClickListener { changerEtatChrono() }
        btnFinSession.setOnClickListener{ arreterSessionEtude() }
    }

    // Méthode pour initialiser les adapters
    private fun initialiserAdapters() {
        // On veut que les recycler view affiche selon un LinearLayout
        recyclerAFaire.layoutManager = LinearLayoutManager(this)
        recyclerFini.layoutManager = LinearLayoutManager(this)

        // initialise l'adapter et on lui passe la liste ainsi que les fonctions nécessaires
        adaptorAFaire = ListeTacheAdaptor(
            listeTacheAFaire,
            { tache -> completerTache(tache) },
            { tache -> supprimerTacheAFaire(tache) },
            {}
        )

        adaptorFini = ListeTacheAdaptor(
            listeTacheFini,
            {},
            { tache -> supprimerTacheFini(tache) },
            { tache -> repeterTache(tache) }
        )

        recyclerAFaire.adapter = adaptorAFaire
        recyclerFini.adapter = adaptorFini
    }

    // Méthode pour compléter une tâche
    private fun completerTache(tache: Taches) {
        listeTacheAFaire.remove(tache) // On l'enlève de la liste à faire
        listeTacheFini.add(tache) // On l'ajoute dans la liste fini
        tache.estFini = true // On met à jour son état
        // On notifie les changements
        adaptorAFaire.mettreAJourDonnee(listeTacheAFaire)
        adaptorFini.mettreAJourDonnee(listeTacheFini)
        tacheCompleter++ // On incrémente le nombre de tâche fini
    }

    // Méthode pour supprimer une tâche à faire
    private fun supprimerTacheAFaire(tache: Taches) {
        listeTacheAFaire.remove(tache)
        adaptorAFaire.mettreAJourDonnee(listeTacheAFaire)
    }

    // Méthode pour supprimer une tâche fini
    private fun supprimerTacheFini(tache: Taches) {
        listeTacheFini.remove(tache)
        adaptorFini.mettreAJourDonnee(listeTacheFini)
    }

    // Méthode pour répéter une tâche
    private fun repeterTache(tache: Taches) {
        // On assure que la tâche à faire ne contient pas déjà la tâche
        if (!listeTacheAFaire.contains(tache)) {
            listeTacheAFaire.add(tache) // On l'ajoute dans à faire
            tache.estFini = false // on met à jour l'état
            listeTacheFini.remove(tache) // on l'enlève de fini
            // On met à jour les données
            adaptorAFaire.mettreAJourDonnee(listeTacheAFaire)
            adaptorFini.mettreAJourDonnee(listeTacheFini)
            tacheCompleter-- // On décrémente le nombre de tâche compléter
        }
    }

    // Méthode pour ajouter une tâche
    private fun ajouterTache(nouvelleTache: Taches) {
        listeTacheAFaire.add(nouvelleTache) // On ajoute la tâche
        adaptorAFaire.mettreAJourDonnee(listeTacheAFaire) // On met à jour le recycler view
        tacheCreer++ // On incrémente le nombre de tâche créer
    }

    // Source: https://medium.com/@praveenjeffri/how-to-create-a-simple-countdown-timer-in-android-using-kotlin-ed7dc781ba7e
    private fun changerEtatChrono() {
        // Si le chrono est en fonction on le met en pause
        if (chronoEnFonction) {
            pauserChrono()
            btnControlTemps.setImageResource(R.drawable.baseline_play_arrow_24)
        } else {
            // Sinon, on le commence/recommence
            commencerChrono()
            btnControlTemps.setImageResource(R.drawable.baseline_pause_24)
        }
    }

    // Méthode pour commencer/recommence le chrono
    private fun commencerChrono() {
        chronoEnFonction = true // Le chrono est en fonction
        chrono = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tempsEcouler++ // À chaque tick on incrémente le tempsÉcouler
                mettreAJourChrono()
            }
            override fun onFinish() {
               // Obligatoirement implémenter
            }
        }.start()
    }

    // Méthode pour mettre le chrono en pause
    private fun pauserChrono() {
        chronoEnFonction = false
        if (::chrono.isInitialized) {
            chrono.cancel()
        }
    }

    // Méthode pour mettre à jour le chrono au niveau visuel
    @SuppressLint("DefaultLocale")
    private fun mettreAJourChrono() {
        // Formattage de temps fourni par ChatGPT
        val heures = (tempsEcouler / 3600).toInt()
        val minutes = ((tempsEcouler % 3600) / 60).toInt()
        val secondes = (tempsEcouler % 60).toInt()

        val tempFormater = String.format("%02d:%02d:%02d", heures, minutes, secondes)
        txtChronometre.text = tempFormater
    }

    // Méthode pour créer un dialogue d'ajout tâche
    private fun activerDialogAjoutTache(){
        // Source: https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.ajouter_tache))

        val champSaisiTache = EditText(this)
        builder.setView(champSaisiTache)

        builder.setPositiveButton(getString(R.string.ajouter)) { _, _ ->
            val titre = champSaisiTache.text.toString()
            if (titre.isNotBlank()) { // Si le titre n'est pas vide...
                val nouvelleTache = Taches(titre, false)
                ajouterTache(nouvelleTache) // On ajoute la tâche
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_titre_tache_invalide), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@SessionEtude, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@SessionEtude, R.color.noir))
                    show()
                }
            }
        }
        builder.setNegativeButton(getString(R.string.annuler)) { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Méthode pour arrêter une session d'étude
    private fun arreterSessionEtude() {
        // Définir une session d'étude pour ajouter
        val sessionEtude = Etude(
            idUtilisateurFK = session.retournerIdUtilisateur(),
            nomCours = coursChoisi,
            nomSessionEtude = txtNomSession.text.toString(),
            tempsEtude = tempsEcouler,
            tacheCompleter = tacheCompleter,
            tacheCreer = tacheCreer
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    arreterMusique() // On arrête la musique
                    pauserChrono() // On arrête le chrono si il est en marche
                    incrementerStreakUtilisateur() // On incrémente le streak de l'utilisateur s'il y a lieu
                }
                // On insert une session d'étude
                etudeDao.insertEtude(sessionEtude)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@SessionEtude, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@SessionEtude, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour arrêter la musique
    private fun arreterMusique() {
        if (lecteurMusique != null) {
            lecteurMusique?.stop()
            lecteurMusique?.release()
            lecteurMusique = null
        }
    }

    // Source: https://www.geeksforgeeks.org/spinner-in-kotlin/
    // Source: https://www.baeldung.com/kotlin/data-class-to-map
    // Méthode pour montrer la dialogue au début de la session
    private fun montrerDialogueDebutSession() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On charge les cours de l'utilisateur
                val mesCours = lesCoursDao.getCoursParIdUtilisateur(session.retournerIdUtilisateur())

                withContext(Dispatchers.Main) {
                    val layout = creerDialogueConfiguration(mesCours) // On crée la dialogue
                    val builder = AlertDialog.Builder(this@SessionEtude)
                        .setTitle(getString(R.string.configuration_etude))
                        .setView(layout)
                        .setCancelable(false) // On ne veut pas que l'utilisateur sort en cliquant à l'extérieur du dialogue

                    initialiserBoutonDialogue(builder, layout)
                    builder.show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@SessionEtude, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@SessionEtude, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour créer le contenu de la dialogue de configuration
    private fun creerDialogueConfiguration(mesCours: List<LesCours>): LinearLayout {
        val layout = LinearLayout(this@SessionEtude)
        layout.orientation = LinearLayout.VERTICAL

        // On prend la valeur de la champs de saisi pour le nom de la session d'étude
        val champSaisiNom = EditText(this@SessionEtude).apply {
            hint = getString(R.string.indice_titre_session_etude)
        }
        layout.addView(champSaisiNom) // On l'ajoute au layout

        val titreSelectionCours = TextView(this@SessionEtude).apply {
            text = getString(R.string.choisir_cours)
        }
        layout.addView(titreSelectionCours) // On l'ajoute au layout

        // On créer le dropdown pour séléction un cours
        val menuSelection = Spinner(this@SessionEtude)
        val adapter = ArrayAdapter(this@SessionEtude, android.R.layout.simple_spinner_item, mesCours.map { it.nomCours })
        menuSelection.adapter = adapter
        // On l'ajoute au layout
        layout.addView(menuSelection)

        return layout
    }

    // Méthode pour initialiser les boutons du dialogue
    private fun initialiserBoutonDialogue(builder: AlertDialog.Builder, layout: LinearLayout) {
        val champSaisiNom = layout.getChildAt(0) as EditText // On cherche les éléments qu'on aurait besoin
        val menuSelection = layout.getChildAt(2) as Spinner

        // Quand l'utilisateur confirme, on initialise le nom du session et le cours qu'il est associé
        builder.setPositiveButton(getString(R.string.confirmer)) { _, _ ->
            val titre = champSaisiNom.text.toString()
            coursChoisi = menuSelection.selectedItem?.toString() ?: getString(R.string.aucun_valeur)

            if (titre.isNotBlank()) {
                txtNomSession.text = titre
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_configuration_etude), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@SessionEtude, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@SessionEtude, R.color.noir))
                    show()
                }
                montrerDialogueDebutSession()
            }
        }

        builder.setNegativeButton(getString(R.string.annuler)) { dialog, _ ->
            dialog.cancel()
            naviguerVersAutrePage(HistoriqueEtude::class.java)
            arreterMusique()
        }
    }

    // Méthode pour incrmenter le streak de l'utilisateur à la fin d'une session d'étude
    private fun incrementerStreakUtilisateur() {
        // Récupère la date courant
        val dateCourant = LocalDate.now()
        val dateCourantFormatter = dateCourant.format(DateTimeFormatter.ISO_LOCAL_DATE)

        lifecycleScope.launch(Dispatchers.IO) {
            var utilisateur = utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())

            try {
                if (utilisateur == null) {
                    return@launch
                }
                // Si la date est null ou différent de la date sauvegarder, on incrémente le streak
                if (utilisateur.dernierDateStreak != dateCourantFormatter || utilisateur.dernierDateStreak == null) {
                    utilisateur = utilisateur.copy(streak =  utilisateur.streak + 1,
                        dernierDateStreak = dateCourantFormatter)

                    utilisateurDao.updateUtilisateur(utilisateur)

                    withContext(Dispatchers.Main) {
                        // On met à jour la session directement
                        session.continueStreakCourant()
                        session.modifierDateStreakCourant(dateCourantFormatter)
                    }
                }
                naviguerVersAutrePage(HistoriqueEtude::class.java)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@SessionEtude, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@SessionEtude, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour naviguer à un autre page
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(this, page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }
}
