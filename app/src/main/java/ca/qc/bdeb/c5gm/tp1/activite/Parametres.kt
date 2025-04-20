package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
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

class Parametres : AppCompatActivity() {

    // Base de données
    private lateinit var firestore: FirebaseFirestore
    private lateinit var utilisateurDao: UtilisateurDao

    // Session
    private lateinit var session: GestionnaireSession

    // Élements d'interface
    private lateinit var champsSaisiNomUtilisateur: EditText
    private lateinit var champSaisiMDP: EditText
    private lateinit var champSaisiMDPConfirmation: EditText
    private lateinit var btnModifier: Button
    private lateinit var btnSupprimer: Button
    private lateinit var btnModeSombre: Switch

    private var infoCompte: Utilisateur? = null
    private var estThemeSombre: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Application du thème selon la préférence utilisateur
        session = GestionnaireSession.getInstance(this)
        estThemeSombre = session.retournerModeSombre()
        setTheme(if (estThemeSombre) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.parametre)

        // Initialisation de la base de donnée
        firestore = Firebase.firestore
        utilisateurDao = UtilisateurDao.getInstance(firestore)

        initialiserElementsInterface()
    }

    override fun onStart() {
        super.onStart()
        btnModeSombre.isChecked = estThemeSombre
        chargerInformationCompte() // On charge les informations quand l'application revient en avant plan
    }

    // Méthode pour initialiser les éléments d'interfaces
    private fun initialiserElementsInterface() {
        champsSaisiNomUtilisateur = findViewById(R.id.nomUtilisateurModif)
        champSaisiMDP = findViewById(R.id.mdpModif)
        champSaisiMDPConfirmation = findViewById(R.id.mdpModifConfirmation)

        btnModifier = findViewById(R.id.btnModifierCompte)
        btnModifier.setOnClickListener { modifierCompte() }

        btnSupprimer = findViewById(R.id.btnSupprimerCompte)
        btnSupprimer.setOnClickListener { supprimerCompte() }

        btnModeSombre = findViewById(R.id.modeSombre)
        btnModeSombre.setOnClickListener { changerThemeApplication() }
    }

    // Méthode pour charger l'information du compte
    private fun chargerInformationCompte() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On charge l'information avec le ID de l'utilisateur connecter
                infoCompte = utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())

                // On affiche un erreur, si pour un raison, on ne trouve pas l'utilisateur
                if (infoCompte == null) {
                    // Affiche une alerte dans un cas d'erreur
                    afficherAlerteErreur(getString(R.string.erreur_info_compte))
                } else {
                    withContext(Dispatchers.Main) {
                        // On met le nom de l'utilisateur actif en indice dans le champs de saisi
                        champsSaisiNomUtilisateur.hint = infoCompte?.nomUtilisateur
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour afficher une alerte
    private fun afficherAlerteErreur(message:String) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(message)
        builder.setPositiveButton(getString(R.string.daccord)) { dialog, which ->
            naviguerVersAutrePage(HistoriqueEtude::class.java)
        }
        builder.show()
    }

    // Méthode pour changer le thème d'application
    private fun changerThemeApplication() {
        estThemeSombre = btnModeSombre.isChecked // On regarde l'état du switch
        // On le sauvegarde dans les préférences utilisateur pour pouvoir changer le thème dans les autres activités
        session.modifierTheme(estThemeSombre)

        val infoMAJ = infoCompte!!.copy(modeSombre = estThemeSombre)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On met a jour l'utilisateur avec les nouvelles informations
                utilisateurDao.updateUtilisateur(infoMAJ)
                withContext(Dispatchers.Main) {
                    // Demande de se reconnecter pour voir les changements
                    afficherAlerteErreur(getString(R.string.demande_reconnexion))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode "helper" pour vérifier les informations avant la modification d'un utilisateur
    private fun verifierInfoModification(nouveauNom: String, nouveauMDP: String, confirmationMDP: String) : Boolean{
        var estValide = true
        // On vérifie si les champs sont vide
        if (nouveauNom.isEmpty() || nouveauMDP.isEmpty() || confirmationMDP.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_champs_manquant), Snackbar.LENGTH_LONG).apply {
                setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                show()
            }
            estValide = false
        }

        // On vérifie si les mots de passe sont la même
        if (nouveauMDP != confirmationMDP) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_mdp_different), Snackbar.LENGTH_LONG).apply {
                setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                show()
            }
            estValide = false
        }
        return estValide
    }

    // Méthode pour modifier l'information du compte
    private fun modifierCompte() {
        val nouveauNom = champsSaisiNomUtilisateur.text.toString()
        val nouveauMDP = champSaisiMDP.text.toString()
        val confirmationMDP = champSaisiMDPConfirmation.text.toString()

        // Appelle de la méthode de vérification
        if (!verifierInfoModification(nouveauNom, nouveauMDP, confirmationMDP)){
            return
        }

        // Information à mettre à jour
        val infoMAJ = Utilisateur(
            idUtilisateurPK = infoCompte!!.idUtilisateurPK,
            nomUtilisateur = if (nouveauNom.isNotEmpty()) nouveauNom else infoCompte?.nomUtilisateur,
            motDePasse = if (nouveauMDP.isNotEmpty()) nouveauMDP else infoCompte?.motDePasse,
            modeSombre = session.retournerModeSombre(),
            dernierDateStreak = infoCompte?.dernierDateStreak,
            streak = infoCompte!!.streak,
            amis = infoCompte!!.amis
        )

        // Lancement du coroutine pour les opérations de base de données
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // On met à jour l'utilisateur
                val utilisateurExistent = utilisateurDao.getUtilisateurParNomUtilisateur(nouveauNom)
                if (utilisateurExistent == null || utilisateurExistent.nomUtilisateur == infoCompte?.nomUtilisateur) {
                    utilisateurDao.updateUtilisateur(infoMAJ)
                    withContext(Dispatchers.Main) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_modif_compte), Snackbar.LENGTH_LONG).apply {
                            setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                            setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                            show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_nom_existant), Snackbar.LENGTH_LONG).apply {
                            setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                            setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                            show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                        setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                        show()
                    }
                }
            }
        }
    }

    // Méthode pour supprimer un compte utilisateur
    private fun supprimerCompte() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(getString(R.string.alerte_suppression_compte))
        builder.setMessage(getString(R.string.question_suppression_compte))

        builder.setPositiveButton(getString(R.string.oui)) { _, _ ->
            infoCompte?.let { utilisateur ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        // Appelle de la fonction pour supprimer un utilisateur dans la base de donnée
                        utilisateurDao.deleteUtilisateur(utilisateur)
                        withContext(Dispatchers.Main) {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.message_supression_compte), Snackbar.LENGTH_LONG).apply {
                                setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                                setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                                show()
                            }
                            naviguerVersAutrePage(Connexion::class.java)
                            finishAffinity()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(findViewById(android.R.id.content), getString(R.string.erreur_operation_interne), Snackbar.LENGTH_LONG).apply {
                                setBackgroundTint(ContextCompat.getColor(this@Parametres, R.color.blanc))
                                setTextColor(ContextCompat.getColor(this@Parametres, R.color.noir))
                                show()
                            }
                        }
                    }
                }
            }
        }
        builder.setNegativeButton(getString(R.string.non)) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    // Méthode pour naviguer dans un autre page
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(this, page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }
}
