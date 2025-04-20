package ca.qc.bdeb.c5gm.tp1.activite

import GestionnaireSession
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.qc.bdeb.c5gm.tp1.R

class Amis : AppCompatActivity() {
    // Session
    lateinit var session: GestionnaireSession

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialiser le thème
        session = GestionnaireSession.getInstance(this)
        setTheme(if (session.retournerModeSombre()) R.style.Base_Theme_Dark else R.style.Base_Theme_Light)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.amis)

    }
}
