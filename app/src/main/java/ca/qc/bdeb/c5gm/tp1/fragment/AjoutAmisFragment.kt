package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.CLIPBOARD_SERVICE
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ca.qc.bdeb.c5gm.tp1.R
import com.google.android.material.snackbar.Snackbar

class AjoutAmisFragment : Fragment() {

    private val amiViewModel: AmisViewModel by activityViewModels()
    private lateinit var session: GestionnaireSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Récupère la session d'utilisateur
        session = GestionnaireSession.getInstance(requireActivity())
        return inflater.inflate(R.layout.fragment_ajout_amis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialisation des composants de l'interface
            val txtIdUtilisateur: TextView = view.findViewById(R.id.idAjoutAmi)
            txtIdUtilisateur.text = session.retournerIdUtilisateur()

            val btnCopier: ImageView = view.findViewById(R.id.btnCopier)
            btnCopier.setOnClickListener { copierTexteDansPressePapier(txtIdUtilisateur.text.toString()) }

            val btnAjoutAmi: ImageView = view.findViewById(R.id.btnAjoutAmi)
            btnAjoutAmi.setOnClickListener { afficherDialogueAjoutAmi() }
        } catch (e: Exception) {
            afficherSnackbar(getString(R.string.erreur_initialisation))
        }
    }

    // Méthode pour copier le text dans le presse papier (copier l'id)
    private fun copierTexteDansPressePapier(texte: String) {
        if (texte.isNotEmpty()) {
            val clipboard = requireActivity().getSystemService(CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager

            val clip = android.content.ClipData.newPlainText("ID utilisateur", texte)
            clipboard.setPrimaryClip(clip)
            afficherSnackbar(getString(R.string.id_copie))

        } else {
            afficherSnackbar(getString(R.string.erreur_id_inconnu))
        }
    }

    // Méthode pour afficher la diaglogue pour afficher un ami
     private fun afficherDialogueAjoutAmi() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        builder.setTitle(getString(R.string.ajouter_ami_titre))

        // Initialise le champ de saisi dans l'interface
        val input = android.widget.EditText(requireActivity())
        input.hint = getString(R.string.entrer_id_ami)
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // On crée un bouton positive
        builder.setPositiveButton(getString(R.string.ajouter)) { dialog, _ ->
            val idAmi = input.text.toString().trim()
            if (idAmi.isNotEmpty()) { // S'il n'est pas vide...
                amiViewModel.ajouterAmi(idAmi, session) // On appelle la méthode du viewModel
            } else {
                afficherSnackbar(getString(R.string.erreur_id_vide))
            }
            dialog.dismiss()
        }

        // Annule la dialogue
        builder.setNegativeButton(getString(R.string.annuler)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    // Méthode pour afficher un snackbar
    private fun afficherSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).apply {
            setBackgroundTint(ContextCompat.getColor(requireActivity(), R.color.blanc))
            setTextColor(ContextCompat.getColor(requireActivity(), R.color.noir))
            show()
        }
    }
}