package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeAmiAdapter

class ListeAmisFragment : Fragment() {
    private val amiViewModel: AmisViewModel by activityViewModels()
    private lateinit var session: GestionnaireSession
    private lateinit var amisAdapter: ListeAmiAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Récupère la session utilisateur
        session = GestionnaireSession.getInstance(requireActivity())
        return inflater.inflate(R.layout.fragment_liste_amis, container, false)
    }

    override fun onResume() {
        super.onResume()
        // On initialise la liste dans le but de le mettre à jour
        // Pour les cas suivant, ajout d'amis ou changement dans les classsement
        amiViewModel.populerAmis(session, estClassement = false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialise le recycler view
        val recyclerAmi: RecyclerView = view.findViewById(R.id.recyclerAmi)
        recyclerAmi.layoutManager = LinearLayoutManager(requireActivity())

        // On observe la liste des données pour des changement
        amiViewModel.amisUtilisateur.observe(viewLifecycleOwner) { amis ->
            amisAdapter = ListeAmiAdapter(amis,
            { utilisateur -> amiViewModel.supprimerAmi(utilisateur, session)}
            )
            recyclerAmi.adapter = amisAdapter
            amisAdapter.mettreAJourListe()
        }
    }
}