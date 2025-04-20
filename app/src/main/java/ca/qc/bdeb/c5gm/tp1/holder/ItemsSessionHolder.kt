package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemsSessionHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val nomSession: TextView
    val nomCours: TextView
    val temps: TextView
    val tacheCreer: TextView
    val tacheCompleter: TextView
    val btnSupprimer: ImageView

    init {
        layout = itemView as ConstraintLayout
        nomSession = itemView.findViewById(R.id.titreSession)
        nomCours = itemView.findViewById(R.id.txtNomCours)
        temps = itemView.findViewById(R.id.txtTempsEtude)
        tacheCreer = itemView.findViewById(R.id.txtTacheCreer)
        tacheCompleter = itemView.findViewById(R.id.txtTacheCompleter)
        btnSupprimer = itemView.findViewById(R.id.btnSupprimerSession)
    }
}