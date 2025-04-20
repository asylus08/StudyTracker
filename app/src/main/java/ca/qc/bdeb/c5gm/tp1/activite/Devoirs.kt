package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ca.qc.bdeb.c5gm.tp1.dao.LesDevoirsDao
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ca.qc.bdeb.c5gm.tp1.R
import androidx.lifecycle.lifecycleScope
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.viewModels
import ca.qc.bdeb.c5gm.tp1.dao.LesCoursDao
import ca.qc.bdeb.c5gm.tp1.fragment.DevoirViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.withContext


class Devoirs : AppCompatActivity() {
    companion object {
        const val REQUEST_CODE_EDIT = 1
    }

    // Initialisation des listes
    private var listeAFaire: MutableList<LesDevoirs> = mutableListOf<LesDevoirs>()
    private var listeCompletee: MutableList<LesDevoirs> = mutableListOf<LesDevoirs>()

    // Initialisation des variables pour les devoirs
    lateinit var imageDevoir: ImageView
    private lateinit var uriImageSelectionnee: Uri
    private val CAMERA_PERMISSION_CODE = 100
    private val REQUEST_CODE_READ_STORAGE = 101
    private var permissionsChecked = false
    private lateinit var prendrePhoto: ActivityResultLauncher<Uri>
    private lateinit var choisirImage: ActivityResultLauncher<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var lesDevoirsDao: LesDevoirsDao
    private val viewModel: DevoirViewModel by viewModels()
    private lateinit var floatingButton: FloatingActionButton

    // Initialisation des variables pour les cours
    lateinit var session: GestionnaireSession
    private lateinit var lesCoursDao: LesCoursDao

    override fun onCreate(savedInstanceState: Bundle?) {
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.devoirs)

        // Initialiser la toolbar, le menu pour la navigation et la base de données
        initialiserElementsInterface()
        // Configurer le lanceur de média
        configurerLanceursDeMedia()

        // Vérifier les permissions une seule fois
        if (!permissionsChecked) {
            // Vérifier les permissions pour accéder à la caméra
            verifierPermissions()
            permissionsChecked = true
        }

        // Configurer le bouton flottant pour ajouter un devoir
        floatingButton = findViewById(R.id.floatingButton)
        floatingButton.setOnClickListener {
            afficherBoiteDialogueAjouterDevoir()
        }
    }

    override fun onStart() {
        super.onStart()
        // Charger les devoirs
        afficherDevoirs(session.retournerIdUtilisateur())
    }

    // Méthode pour inisialiser les éléments de l'interface
    private fun initialiserElementsInterface(){
        firestore = Firebase.firestore
        lesDevoirsDao = LesDevoirsDao.getInstance(firestore)
        lesCoursDao = LesCoursDao.getInstance(firestore)

        // Initialisation du ViewModel
        val userId = session.retournerIdUtilisateur()
        viewModel.chargerDevoirs(lesDevoirsDao, userId)
    }


    // Méthode pour configurer le lanceur de média
    private fun configurerLanceursDeMedia() {
        prendrePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageDevoir.setImageURI(uriImageSelectionnee)
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_capture_photo), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                    show()
                }
            }
        }

        choisirImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    imageDevoir.setImageURI(it)
                    uriImageSelectionnee = it
                } catch (e: SecurityException) {
                    imageDevoir.setImageResource(R.drawable.baseline_assignment_24)
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_selection_image), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                        show()
                    }
                }
            } ?: run {
                imageDevoir.setImageResource(R.drawable.baseline_assignment_24)
            }
        }
    }

    // Méthode pour afficher la boite de dialogue qui permet d'ajouter un devoir
    private fun afficherBoiteDialogueAjouterDevoir() {
        // Choisir le style du dialogue en fonction du mode (sombre ou clair)
        val dialogStyle = if (session.retournerModeSombre()) {
            R.style.CustomAlertDialogModeSombre
        } else {
            R.style.CustomAlertDialog
        }

        lifecycleScope.launch(Dispatchers.IO) {
            // Source du code : https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
            // Source du code : https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
            withContext(Dispatchers.Main) {
                // Utiliser le style `dialogStyle` dans le constructeur
                val constructeur = AlertDialog.Builder(this@Devoirs, dialogStyle)
                val remplisseur = layoutInflater
                val miseEnPageDialogue = remplisseur.inflate(R.layout.ajout_devoirs, null)

                // Initialiser les vues dans le layout
                val editTextNomCours = miseEnPageDialogue.findViewById<EditText>(R.id.editTextCours)
                val editTextDescription = miseEnPageDialogue.findViewById<EditText>(R.id.editTextDescription)
                val spinnerPriorite = miseEnPageDialogue.findViewById<Spinner>(R.id.spinnerIndicePriorite)
                imageDevoir = miseEnPageDialogue.findViewById(R.id.imageDevoir)
                val btnAjouterImage = miseEnPageDialogue.findViewById<Button>(R.id.btnAjouterImage)
                val btnAjouterDevoir = miseEnPageDialogue.findViewById<Button>(R.id.btnAjouterDevoir)
                val btnAnnuler = miseEnPageDialogue.findViewById<Button>(R.id.btnAnnuler)
                var imageAjoutee = false

                // Définir le titre de l'AlertDialog ici
                constructeur.setTitle(getString(R.string.ajouter_devoir))
                constructeur.setView(miseEnPageDialogue)
                val dialog = constructeur.create()

                // Bouton Ajouter - avec vérification
                btnAjouterDevoir.setOnClickListener {
                    val nomDevoir = editTextNomCours.text.toString().trim()
                    val descriptionText = editTextDescription.text.toString().trim()

                    when {
                        nomDevoir.isEmpty() -> {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_nom_vide), Snackbar.LENGTH_LONG).apply {
                                setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                                setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                                show()
                            }
                        }
                        descriptionText.isEmpty() -> {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_description_vide), Snackbar.LENGTH_LONG).apply {
                                setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                                setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                                show()
                            }
                        }
                        !imageAjoutee -> {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_image_vide), Snackbar.LENGTH_LONG).apply {
                                setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                                setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                                show()
                            }
                        }
                        else -> {
                            val prioriteSelectionnee = spinnerPriorite.selectedItem.toString()

                            if (prioriteSelectionnee == null || prioriteSelectionnee.isEmpty()) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_priorite_vide), Snackbar.LENGTH_LONG).apply {
                                    setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                                    setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                                    show()
                                }
                                return@setOnClickListener
                            }

                            if (uriImageSelectionnee == null) {
                                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_image_vide), Snackbar.LENGTH_LONG).apply {
                                    setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                                    setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                                    show()
                                }
                                return@setOnClickListener
                            }

                            ajouterDevoir(R.drawable.baseline_image_24, nomDevoir, descriptionText.ifEmpty { getString(R.string.description_defaut) }, uriImageSelectionnee?.toString(), prioriteSelectionnee)
                            dialog.dismiss()
                        }
                    }
                }

                // Bouton Annuler - Ferme la boîte de dialogue sans vérification
                btnAnnuler.setOnClickListener {
                    dialog.dismiss()
                }

                // Configuration du bouton pour ajouter une image
                btnAjouterImage.setOnClickListener {
                    afficherOptionsPhoto()
                    imageAjoutee = true
                    btnAjouterDevoir.isEnabled = true
                }

                dialog.show()
            }
        }
    }

    // Méthode pour afficher les options de sélection d'image
    private fun afficherOptionsPhoto() {
        val options = arrayOf(getString(R.string.prendre_photo), getString(R.string.choisir_galerie))
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.selection_option_image))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> lancerCamera()
                1 -> choisirImage.launch("image/*")
            }
        }
        builder.show()
    }

    // Méthode pour lancer la caméra
    private fun lancerCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            val uriPhoto = creerUriPhoto()
            uriImageSelectionnee = uriPhoto
            prendrePhoto.launch(uriPhoto)
        }
    }

    // Méthode pour créer un URI pour une photo
    private fun creerUriPhoto(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile: File = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
        return FileProvider.getUriForFile(this, "ca.qc.bdeb.c5gm.tp1.fileprovider", photoFile)
    }

    // Méthode pour ajouter un devoir
    private fun ajouterDevoir(imgDevoir: Int, nomDevoir: String, description: String, imageUri: String?, indicePriorite: String) {
        // On crée un instance de devoir
        val nouveauDevoir = LesDevoirs(
            idUtilisateurFK = session.retournerIdUtilisateur(),
            estComplete = false,
            imgDevoir = imgDevoir,
            nomDevoir = nomDevoir,
            description = description,
            imageUri = imageUri,
            indicePriorite  = indicePriorite
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                lesDevoirsDao.insertDevoir(nouveauDevoir)
                withContext(Dispatchers.Main) {
                    viewModel.ajouterDevoir(nouveauDevoir)
                    viewModel.chargerDevoirs(lesDevoirsDao, session.retournerIdUtilisateur())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_ajout_devoir), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour afficher les devoirs dans les listes
    private fun afficherDevoirs(idUtilisateur: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val listeDevoirs = lesDevoirsDao.getDevoirsParIdUtilisateurs(idUtilisateur)
            runOnUiThread {
                listeAFaire.clear()
                listeCompletee.clear()

                for (devoir in listeDevoirs) {
                    val devoirItem = LesDevoirs(
                        idDevoirPK = devoir.idDevoirPK?.toString(),
                        idUtilisateurFK = session.retournerIdUtilisateur(),
                        nomDevoir = devoir.nomDevoir,
                        imgDevoir = R.drawable.baseline_image_24,
                        estComplete = devoir.estComplete,
                        description = devoir.description,
                        imageUri = devoir.imageUri,
                        indicePriorite = devoir.indicePriorite
                    )

                    if (devoir.estComplete) {
                        listeCompletee.add(devoirItem)
                    } else {
                        listeAFaire.add(devoirItem)
                    }
                }

                Log.d("Devoirs", "Liste des devoirs à faire : ${listeAFaire.size}")
                Log.d("Devoirs", "Liste des devoirs complétés : ${listeCompletee.size}")

            }
        }
    }

    // Méthode pour changer le devoir de liste une fois qu'il est completé
    fun completerDevoir(devoir: LesDevoirs) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("Devoirs", "ID du devoir pour la complétion : ${devoir.idDevoirPK}")
            devoir.idDevoirPK?.let { id ->
                val devoirComplet = lesDevoirsDao.getDevoirParId(id)
                devoirComplet?.let {
                    it.estComplete = devoir.estComplete
                    lesDevoirsDao.updateDevoir(it)

                    withContext(Dispatchers.Main) {
                        viewModel.chargerDevoirs(lesDevoirsDao, session.retournerIdUtilisateur())
                    }
                } ?: runOnUiThread {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_devoir_introuvable), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                        show()
                    }
                }
            } ?: runOnUiThread {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_id_devoir_introuvable), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                    show()
                }
            }
        }
    }


    // Méthode pour confirmer la supression d'un devoir
    fun confirmerSuppressionDevoir(devoir: LesDevoirs) {
        // Source du code : https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin
        // Source du code : https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/
        val dialogStyle = if (session.retournerModeSombre()) {
            R.style.CustomAlertDialogModeSombre
        } else {
            R.style.CustomAlertDialog
        }

        AlertDialog.Builder(this, dialogStyle).apply {
            setTitle(getString(R.string.supprimer))
            setMessage(getString(R.string.confirmation_suppression))
            setPositiveButton(getString(R.string.oui)) { _, _ ->
                supprimerDevoir(devoir)
            }
            setNegativeButton(getString(R.string.non), null)
        }.show()
    }

    // Méthode pour supprimer un devoir
    fun supprimerDevoir(devoir: LesDevoirs) {
        CoroutineScope(Dispatchers.IO).launch {
            devoir.idDevoirPK?.let { id ->
                val devoirComplet = lesDevoirsDao.getDevoirParId(id)
                devoirComplet?.let {
                    lesDevoirsDao.deleteDevoir(it)
                    runOnUiThread {
                        if (listeAFaire.contains(devoir)) {
                            listeAFaire.remove(devoir)
                        } else if (listeCompletee.contains(devoir)) {
                            listeCompletee.remove(devoir)
                        }
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.confirmation_devoir_supprime), Snackbar.LENGTH_LONG).apply {
                            setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                            setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                            show()
                        }
                        viewModel.chargerDevoirs(lesDevoirsDao, session.retournerIdUtilisateur()) // Resynchroniser
                    }
                } ?: runOnUiThread {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_devoir_introuvable), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                        show()
                    }
                }
            } ?: runOnUiThread {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_id_devoir_introuvable), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@Devoirs, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@Devoirs, R.color.noir))
                    show()
                }
            }
        }
    }

    // Méthode pour vérifier les permissions
    private fun verifierPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                verifierEtDemanderPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                verifierEtDemanderPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_CODE_READ_STORAGE)
            }
        }
    }

    // Méthode pour demander les permissions
    private fun verifierEtDemanderPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }


    // Méthode pour gérer le résultat des demandes de permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choisirImage.launch("image/*")
            }
        }
    }

    // Méthode pour gérer le retour des résultats après qu'une activité ait été modifiée
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_EDIT && resultCode == RESULT_OK) {
            afficherDevoirs(session.retournerIdUtilisateur())
        }
    }

    // Méthode pour aller à la page pour modifier les informations d'un devoir
    fun allerAPageModifierInformationDevoir(devoir: LesDevoirs) {
        val intent = Intent(this, ModifierInformationDevoir::class.java).apply {
            putExtra("devoirId", devoir.idDevoirPK)
            putExtra("devoirNom", devoir.nomDevoir)
            putExtra("devoirImageUri", devoir.imageUri)
            putExtra("devoirDescription", devoir.description)
        }
        this.startActivityForResult(intent, Devoirs.REQUEST_CODE_EDIT)
    }
}
