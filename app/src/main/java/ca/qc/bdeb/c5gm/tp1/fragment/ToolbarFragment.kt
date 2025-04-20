package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.activite.APropos
import ca.qc.bdeb.c5gm.tp1.activite.Amis
import ca.qc.bdeb.c5gm.tp1.activite.Connexion
import ca.qc.bdeb.c5gm.tp1.activite.Parametres
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar


class ToolbarFragment : Fragment() {
    // Source: https://stackoverflow.com/questions/71917856/sethasoptionsmenuboolean-unit-is-deprecated-deprecated-in-java
    private lateinit var session: GestionnaireSession

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        session = GestionnaireSession.getInstance(requireActivity())
        return inflater.inflate(R.layout.fragment_toolbar, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().invalidateOptionsMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ligne 38-39 est fournie par ChatGPT. Pas trouver de solution sur StackOverflow
        val toolbar: MaterialToolbar = view.findViewById(R.id.toolbarFragment)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.activity_main_menu, menu)
                initialiserCompteurStreak(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.iconeParametre -> {
                        naviguerVersAutrePage(Parametres::class.java)
                        true
                    }

                    R.id.APropos -> {
                        naviguerVersAutrePage(APropos::class.java)
                        true
                    }

                    R.id.deconnexion -> {
                        session.deconnexion(requireActivity())
                        view.let {
                            Snackbar.make(
                                it,
                                getString(R.string.message_deconnexion),
                                Snackbar.LENGTH_LONG
                            ).apply {
                                setBackgroundTint(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.blanc
                                    )
                                )
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.noir))
                                show()
                            }
                        }
                        naviguerVersAutrePage(Connexion::class.java)
                        requireActivity().finishAffinity()
                        true
                    }

                    R.id.Amis -> {
                        naviguerVersAutrePage(Amis::class.java)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun naviguerVersAutrePage(page: Class<*>) {
        val pageANaviguer = Intent(requireContext(), page)
        pageANaviguer.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(pageANaviguer)
    }

    private fun initialiserCompteurStreak(menu: Menu) {
        val streak = menu.findItem(R.id.streak)
        val streakView = streak.actionView

        val imageFeu = streakView?.findViewById<ImageView>(R.id.iconeFeu)
        val txtNumeroStreak = streakView?.findViewById<TextView>(R.id.numeroStreak)

        if (streakView == null) {
            Log.e("StreakDebug", "Le streakview est null.")
        }
        
        txtNumeroStreak?.text = session.retournerStreakCourant().toString()
        Log.d("STREAK", session.retournerAContinuerStreak().toString())
        imageFeu?.setImageResource(
            if (session.retournerAContinuerStreak()) R.drawable.feu
            else R.drawable.feu_eteint
        )
        return

    }
}
