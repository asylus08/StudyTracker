package ca.qc.bdeb.c5gm.tp1.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.activite.Devoirs
import ca.qc.bdeb.c5gm.tp1.adaptor.ListeDevoirsAdaptor
import ca.qc.bdeb.c5gm.tp1.dao.LesDevoirsDao
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DevoirsAFaireFragment : Fragment() {
    private val viewModel: DevoirViewModel by activityViewModels()
    private var listeDevoirsAFaire: MutableList<LesDevoirs> = mutableListOf<LesDevoirs>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var lesDevoirsDao: LesDevoirsDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devoirs_a_faire, container, false)
    }

    override fun onStart() {
        super.onStart()
        firestore = Firebase.firestore
        lesDevoirsDao = LesDevoirsDao.getInstance(firestore)
        val userId = (requireActivity() as Devoirs).session.retournerIdUtilisateur()
        viewModel.chargerDevoirs(lesDevoirsDao, userId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation du RecyclerView
        val recyclerAFaire = view.findViewById<RecyclerView>(R.id.recyclerAFaire)
        recyclerAFaire.layoutManager = LinearLayoutManager(context)

        // Créez une instance de l'adaptateur avec une liste vide
        val adapter = ListeDevoirsAdaptor(
            context = requireContext(),
            devoirs = listeDevoirsAFaire,
            completerDevoir = { devoir -> (requireActivity() as Devoirs).completerDevoir(devoir) },
            modifierDevoir = { devoir -> (requireActivity() as Devoirs).allerAPageModifierInformationDevoir(devoir) }
        )

        recyclerAFaire.adapter = adapter

        // Observez les données
        viewModel.listeAFaire.observe(viewLifecycleOwner) { devoirs ->
            //adapter.mettreAJourDevoirs(devoirs)
            adapter.mettreAJourDevoirs(viewModel.listeAFaire.value ?: listOf())
        }
    }
}