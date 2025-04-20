package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemDevoirsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val nomDevoir: TextView
    val imageDevoir: ImageView
    val description: TextView
    val indicePriorite: TextView
    val btnModifier: Button
    val btnSupprimer : Button
    val checkBox: CheckBox

    init {
        layout = itemView as ConstraintLayout
        nomDevoir = itemView.findViewById(R.id.nomDevoir)
        imageDevoir = itemView.findViewById(R.id.imgCours)
        description = itemView.findViewById(R.id.description)
        indicePriorite = itemView.findViewById(R.id.indicePriorite)
        btnModifier = itemView.findViewById(R.id.btnModifierDevoir)
        btnSupprimer = itemView.findViewById(R.id.btnSupprimerDevoir)
        checkBox = itemView.findViewById(R.id.checkBoxComplete)
    }
}