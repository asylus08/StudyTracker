package ca.qc.bdeb.c5gm.tp1.holder

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R

class ItemClassementHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val layout: ConstraintLayout
    val classement: TextView
    val nomAmi: TextView
    val compteurStreak: TextView

    init {
        layout = itemView as ConstraintLayout
        classement = itemView.findViewById(R.id.classement)
        nomAmi = itemView.findViewById(R.id.nomClassement)
        compteurStreak = itemView.findViewById(R.id.streakClassement)
    }
}