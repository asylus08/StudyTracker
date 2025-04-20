package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemsCoursHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val nomCours: TextView
    val btnSupprimer: ImageView

    init {
        layout = itemView as ConstraintLayout
        nomCours = itemView.findViewById(R.id.txtNomCours)
        btnSupprimer = itemView.findViewById(R.id.btnSupprimerCours)
    }

}