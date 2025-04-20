package ca.qc.bdeb.c5gm.tp1.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class DevoirsCompletesFragment : Fragment() {
    private val viewModel: DevoirViewModel by activityViewModels()
    private var listeDevoirsComplete: MutableList<LesDevoirs> = mutableListOf<LesDevoirs>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var lesDevoirsDao: LesDevoirsDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devoirs_completes, container, false)
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
        val recyclerComplete = view.findViewById<RecyclerView>(R.id.recyclerComplete)
        recyclerComplete.layoutManager = LinearLayoutManager(context)

        // Créez une instance de l'adaptateur avec une liste vide
        val adapter = ListeDevoirsAdaptor(
            context = requireContext(),
            devoirs = listeDevoirsComplete,
            completerDevoir = { devoir -> (requireActivity() as Devoirs).completerDevoir(devoir) },
            modifierDevoir = { devoir -> (requireActivity() as Devoirs).allerAPageModifierInformationDevoir(devoir) }
        )

        recyclerComplete.adapter = adapter

        // Observez les données
        viewModel.listeCompletee.observe(viewLifecycleOwner) { devoirs ->
            adapter.mettreAJourDevoirs(devoirs)
        }
    }
}