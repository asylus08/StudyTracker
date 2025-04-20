package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import ca.qc.bdeb.c5gm.tp1.R
import com.google.android.material.snackbar.Snackbar


class TriviaFragment : Fragment() {

    private val triviaViewModel: TriviaViewModel by activityViewModels()
    private lateinit var session: GestionnaireSession
    lateinit var btnConfirmerReponse: Button
    lateinit var txtQuestion: TextView
    lateinit var saisiReponse: EditText
    lateinit var btnProchainQuestion: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Récupère la session
        session =  GestionnaireSession.getInstance(requireActivity())
        return inflater.inflate(R.layout.fragment_trivia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser les éléments de l'interface
        txtQuestion = view.findViewById(R.id.question)
        saisiReponse = view.findViewById(R.id.saisiReponse)

        btnProchainQuestion = view.findViewById(R.id.btnProchaineQuestion)
        btnProchainQuestion.setOnClickListener { choisirCategorie() }

        btnConfirmerReponse = view.findViewById(R.id.btnConfirmeReponse)
        btnConfirmerReponse.setOnClickListener{ validerReponseUtilisateur(saisiReponse.text.toString()) }

        // On observe le changement de question
        triviaViewModel.question.observe(viewLifecycleOwner) { question ->
            if (question != null) {
                txtQuestion.text = question.question
            }
        }
        // On génére une question
        triviaViewModel.genererQuestion("", view)
    }

    // Méthode pour créer un spinner pour choisir un catégory
    private fun creerSpinnerCategory(): Spinner {
        val spinner = Spinner(requireActivity())
        val categories = resources.getStringArray(R.array.categorie)

        val adapter = ArrayAdapter(requireActivity(), R.layout.spinner, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        return spinner
    }

    private fun choisirCategorie() {
        // On remet la bouton de confirmation a true
        btnConfirmerReponse.isEnabled = true
        saisiReponse.setText("")
        val spinnerCategorie = creerSpinnerCategory()

        // On crée une dialogue pour afficher les catégories
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setTitle(getString(R.string.choisir_categorie))
            .setView(spinnerCategorie)
            .setPositiveButton(getString(R.string.confirmer)) { dialog, _ ->
                val categorieSelectionner = spinnerCategorie.selectedItem as String
                if (categorieSelectionner.isNotEmpty()) {
                    // Si la catégorie est valide, on va générer une question avec l'API
                    view?.let { triviaViewModel.genererQuestion(categorieSelectionner, it) }
                } else {
                    view?.let { Snackbar.make(it, getString(R.string.categorie_invalide), Snackbar.LENGTH_LONG).apply {
                        setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blanc))
                        setTextColor(ContextCompat.getColor(requireContext(), R.color.noir))
                        show()
                    } }
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.annuler)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    // Méthode pour valider la réponse utilisateur
    private fun validerReponseUtilisateur(reponseUtilisateur: String) {
        // On désactive le bouton pour éviter du "spam".
        // C'est pour éviter de "farm" des streak à l'infini si on a la bonne réponse
        btnConfirmerReponse.isEnabled = false
        if (reponseUtilisateur.trim().isEmpty()) {
            view?.let { Snackbar.make(it, getString(R.string.reponse_invalide), Snackbar.LENGTH_LONG).apply {
                setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blanc))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.noir))
                show()
            } }
        } else {
            val resultat = triviaViewModel.validerQuestion(reponseUtilisateur, session)

            // Après avoir évaluer le résultat, on l'affiche
            val message = if (resultat) {
                getString(R.string.bonne_reponse)
            } else {
                getString(R.string.mauvaise_reponse)
            }

            // Vérifie également que la vue est non-nulle avant de montrer le Snackbar
            view?.let { rootView ->
                Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).apply {
                    setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.blanc))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.noir))
                    show()
                }
            }
        }
    }

}