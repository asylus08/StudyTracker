package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemsTacheHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val titre: TextView
    val btnFini: ImageView
    val btnPoubelle: ImageView
    val btnRepeter: ImageView

    init {
        layout = itemView as ConstraintLayout
        titre = itemView.findViewById(R.id.txtNomTache)
        btnPoubelle = itemView.findViewById(R.id.btnPoubelle)
        btnFini = itemView.findViewById(R.id.btnFini)
        btnRepeter = itemView.findViewById(R.id.btnRepeter)
    }
}