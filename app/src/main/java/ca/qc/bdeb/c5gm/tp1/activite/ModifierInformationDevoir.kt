package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.dao.LesDevoirsDao
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ModifierInformationDevoir : AppCompatActivity() {
    // Initialisation des variables pour les devoirs
    private lateinit var nomDevoir: EditText
    private lateinit var descriptionDevoir: EditText
    private lateinit var btnEnregistrer: Button
    private lateinit var btnAnnuler: Button
    private lateinit var imgDevoir: ImageView
    private lateinit var iconeCamera: FloatingActionButton
    private lateinit var spinnerPriorite: Spinner

    // Initialisation des variables pour les photos et images
    private var uriImageSelectionnee: Uri? = null
    private val CAMERA_PERMISSION_CODE = 100
    private lateinit var prendrePhoto: ActivityResultLauncher<Uri>
    private lateinit var choisirImage: ActivityResultLauncher<String>

    // Initialisation des variables pour la base de données
    private lateinit var lesDevoirsDao: LesDevoirsDao
    private var devoirId: String = ""  // ID du devoir
    private lateinit var session: GestionnaireSession
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.modifier_information_devoir)

        // Initialiser les vues
        initialiserVue()
        // Initialiser la base de données
        initialiserBaseDeDonnees()
        // Récupérer les données transmises
        recupererDonnees()
        // Configurer les boutons
        configurerBoutons()
        // Configurer le spinner de priorité
        configurerSpinnerDePriorite()
        // Configurer le lanceur de média
        configurerLanceursDeMedia()
    }

    // Méthode pour initialiser les vues
    private fun initialiserVue(){
        nomDevoir = findViewById(R.id.nomDevoir)
        descriptionDevoir = findViewById(R.id.descriptionDevoir)
        imgDevoir = findViewById(R.id.imgDescriptionDevoir)
        btnAnnuler = findViewById(R.id.btnAnnuler)
        btnEnregistrer = findViewById(R.id.btnEnregister)
        iconeCamera = findViewById(R.id.iconeCamera)
        spinnerPriorite = findViewById(R.id.spinnerPriorite)
    }

    // Méthode pour initialiser la base de données
    private fun initialiserBaseDeDonnees(){
        firestore = Firebase.firestore
        lesDevoirsDao = LesDevoirsDao.getInstance(firestore)
    }

    // Méthode pour récupérer les données
    private fun recupererDonnees(){
        devoirId = intent.getStringExtra("devoirId").toString()
        val devoirNom = intent.getStringExtra("devoirNom")
        val devoirDescription = intent.getStringExtra("devoirDescription")
        val devoirPriorite = intent.getStringExtra("devoirPriorite") ?: "Normale"
        val imageUriString = intent.getStringExtra("devoirImageUri")

        // Afficher les données
        nomDevoir.setText(devoirNom)
        descriptionDevoir.setText(devoirDescription)

        // Afficher l'image si l'URI est présent
        if (!imageUriString.isNullOrEmpty()) {
            uriImageSelectionnee = Uri.parse(imageUriString)
            imgDevoir.setImageURI(uriImageSelectionnee)
        } else {
            imgDevoir.setImageResource(R.drawable.baseline_image_24) // Image par défaut
        }
    }

    // Méthode pour configurer les boutons
    private fun configurerBoutons(){
        iconeCamera.setOnClickListener { afficherOptionsPhoto() }
        btnEnregistrer.setOnClickListener { enregistrerModifications() }
        btnAnnuler.setOnClickListener { finish() }
    }

    // Méthode pour configurer le spinner qui permet de gérer l'indice de priorité
    private fun configurerSpinnerDePriorite(){
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.priorite_array,
            R.layout.spinner
        )
        adapter.setDropDownViewResource(R.layout.spinner)
        spinnerPriorite.adapter = adapter
    }

    // Méthode pour configurer le lanceur de média
    private fun configurerLanceursDeMedia() {
        choisirImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    uriImageSelectionnee = it
                    imgDevoir.setImageURI(it)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Snackbar.make(findViewById(android.R.id.content), "Erreur d'accès à l'image sélectionnée", Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.noir))
                        show()
                    }
                }
            }
        }

        prendrePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && uriImageSelectionnee != null) {
                imgDevoir.setImageURI(uriImageSelectionnee)
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_capture_photo), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.noir))
                    show()
                }
            }
        }
    }

    // Méthode pour afficher si on veut prendre une photo ou si on choisit à partir de la galerie
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

    // Méthode pour créer le URI pour la photo
    private fun creerUriPhoto(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_$timeStamp.jpg")
        return FileProvider.getUriForFile(this, "ca.qc.bdeb.c5gm.tp1.fileprovider", photoFile)
    }

    // Méthode pour accepter les permissions nécessaires pour accéder aux photos et à la caméra
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lancerCamera()
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.permission_camera_refusee), Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.blanc))
                    setTextColor(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.noir))
                    show()
                }
            }
        }
    }

    // Méthode pour enregistrer les modifications apportées
    private fun enregistrerModifications() {
        val nouveauNom = nomDevoir.text.toString()
        val nouvelleDescription = descriptionDevoir.text.toString()
        val nouvellePriorite = spinnerPriorite.selectedItem.toString()

        if (nouveauNom.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_nom_devoir_vide), Snackbar.LENGTH_LONG).apply {
                setBackgroundTint(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.blanc))
                setTextColor(ContextCompat.getColor(this@ModifierInformationDevoir, R.color.noir))
                show()
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            // Mettez à jour le devoir avec l'ID du cours
            lesDevoirsDao.updateDevoir(
                LesDevoirs(
                    idDevoirPK = devoirId,
                    idUtilisateurFK = session.retournerIdUtilisateur(),
                    estComplete = false,
                    nomDevoir = nouveauNom,
                    imgDevoir = R.drawable.baseline_image_24,
                    description = nouvelleDescription,
                    imageUri = uriImageSelectionnee?.toString(),
                    indicePriorite = nouvellePriorite
                )
            )

            // Retourner les données modifiées
            val resultIntent = Intent().apply {
                putExtra("devoirId", devoirId)
                putExtra("nouveauNomDevoir", nouveauNom)
                putExtra("nouvelleDescriptionDevoir", nouvelleDescription)
                putExtra("nouvellePriorite", nouvellePriorite)
                putExtra("devoirImageUri", uriImageSelectionnee?.toString())
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
