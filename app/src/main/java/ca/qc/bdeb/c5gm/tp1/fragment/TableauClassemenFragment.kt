package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeClassementAdapter

class TableauClassemenFragment : Fragment() {
    // Même principe que la class "ListeAmisFragment"
    private val amisViewModel: AmisViewModel by activityViewModels()
    private lateinit var session: GestionnaireSession
    private lateinit var classementAdapter: ListeClassementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        session = GestionnaireSession.getInstance(requireActivity())
        return inflater.inflate(R.layout.fragment_tableau_classemen, container, false)
    }

    override fun onResume() {
        super.onResume()
        // On initialise la liste mais dans le but de le classer, voir code du view model
        amisViewModel.populerAmis(session, estClassement = true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerClassement: RecyclerView = view.findViewById(R.id.recyclerClassement)
        recyclerClassement.layoutManager = LinearLayoutManager(requireActivity())

        amisViewModel.amisUtilisateur.observe(viewLifecycleOwner) { amis ->
            classementAdapter = ListeClassementAdapter(amis)
            recyclerClassement.adapter = classementAdapter
            classementAdapter.trierClassement() // On trie le classement
        }
    }

    override fun onStart() {
        super.onStart()
        if (::classementAdapter.isInitialized){
            classementAdapter.trierClassement() // On trie le classement
        }
    }
}
