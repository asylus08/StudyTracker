package ca.qc.bdeb.c5gm.tp1.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.entite.LesCours
import ca.qc.bdeb.c5gm.tp1.holder.ItemsCoursHolder

class ListeCoursAdaptor(
    private var listeCours: List<LesCours>, // Liste contenant les cours
    private val supprimerCours: (LesCours) -> Unit, // Méthode pour supprimer un cours
): RecyclerView.Adapter<ItemsCoursHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsCoursHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liste_cours, parent, false)
        return ItemsCoursHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsCoursHolder, position: Int) {
        val cours = listeCours[position]

        holder.nomCours.text = cours.nomCours
        // On donne la méthode passer au bouton du holder.
        holder.btnSupprimer.setOnClickListener { supprimerCours(cours) }
    }

    override fun getItemCount(): Int = listeCours.size

    // Méthode pour mettre à jour le recycler view
     fun mettreAJourDonnee(LesCoursAJour: List<LesCours>) {
        listeCours = LesCoursAJour
        notifyDataSetChanged()
    }
}