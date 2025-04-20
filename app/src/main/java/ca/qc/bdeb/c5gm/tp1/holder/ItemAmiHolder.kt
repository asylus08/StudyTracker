package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemAmiHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val nomAmi: TextView
    val btnSupprimer : ImageView

    init {
        layout = itemView as ConstraintLayout
        nomAmi = itemView.findViewById(R.id.idAmi)
        btnSupprimer = itemView.findViewById(R.id.btnSupressionAmi)
    }
}