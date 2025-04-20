package ca.qc.bdeb.c5gm.tp1.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.activite.Cours
import ca.qc.bdeb.c5gm.tp1.activite.Devoirs
import ca.qc.bdeb.c5gm.tp1.activite.Divers
import ca.qc.bdeb.c5gm.tp1.activite.HistoriqueEtude

class NavigationFragment : Fragment() {
    // Source: https://abhiandroid.com/ui/fragment#gsc.tab=0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Crée un view
        val view = inflater.inflate(R.layout.fragment_navigation, container, false)

        // On utilise le view pour créer des boutons
        view.findViewById<LinearLayout>(R.id.versHistoriqueEtude).setOnClickListener {
            naviguerVersAutrePage(HistoriqueEtude::class.java)
        }

        view.findViewById<LinearLayout>(R.id.versCours).setOnClickListener {
            naviguerVersAutrePage(Cours::class.java)
        }

        view.findViewById<LinearLayout>(R.id.versDevoirs).setOnClickListener {
            naviguerVersAutrePage(Devoirs::class.java)
        }

        view.findViewById<LinearLayout>(R.id.versDivers).setOnClickListener{
            naviguerVersAutrePage(Divers::class.java)
        }
        return view
    }

    // Méthode pour naviger vers un autre activité
    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(requireActivity(), page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }

}
