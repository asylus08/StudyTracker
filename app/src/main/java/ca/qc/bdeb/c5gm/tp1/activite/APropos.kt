package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.qc.bdeb.c5gm.tp1.R

class APropos : AppCompatActivity() {
    // Singleton qui va sauvegarder les préférences utilisateurs
    private lateinit var session: GestionnaireSession

    override fun onCreate(savedInstanceState: Bundle?) {
        session = GestionnaireSession.getInstance(this)
        // Initialiser le thème dans le onCreate. (Voir read.me pour explication)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.a_propos)
    }
}