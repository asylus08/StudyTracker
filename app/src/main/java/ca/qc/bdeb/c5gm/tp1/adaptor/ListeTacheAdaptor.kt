
package ca.qc.bdeb.c5gm.tp1.adaptor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.Taches
import ca.qc.bdeb.c5gm.tp1.holder.ItemsTacheHolder

class ListeTacheAdaptor(
    private var lesTaches: List<Taches>, // Liste contenant les tâches
    private val completerTache: (Taches) -> Unit, // Méthode pour completer une tâche
    private val supprimerTache: (Taches) -> Unit, // Méthode pour supprimer une tâche
    private val repeterTache: (Taches) -> Unit, // Méthode pour répéter une tâche
) : RecyclerView.Adapter<ItemsTacheHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsTacheHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liste_taches, parent, false)
        return ItemsTacheHolder(view)
    }

    override fun onBindViewHolder(holder: ItemsTacheHolder, position: Int) {
        val tache = lesTaches[position]
        holder.titre.text = tache.titre

        holder.btnPoubelle.setOnClickListener { supprimerTache(tache) }

        // Dans le cas qu'une tâche est fini...
        if (tache.estFini) {
            holder.btnRepeter.visibility = View.VISIBLE // On met le bouton répéter visible
            holder.btnRepeter.isEnabled = true // Il est maintenant accessible
            holder.btnFini.visibility = View.GONE // le bouton est invisible
            holder.btnFini.isEnabled = false // Il n'y pas d'accès à ce bouton

            holder.btnRepeter.setOnClickListener { repeterTache(tache) }
        } else {
            // Sinon, c'est l'inverse
            holder.btnFini.visibility = View.VISIBLE
            holder.btnFini.isEnabled = true
            holder.btnRepeter.visibility = View.GONE
            holder.btnRepeter.isEnabled = false
            holder.btnFini.setOnClickListener { completerTache(tache) }
        }
    }
    override fun getItemCount(): Int = lesTaches.size

    // Méthode pour mettre à jour le recycler view
     fun mettreAJourDonnee(lesTachesAJour: List<Taches>) {
        lesTaches = lesTachesAJour
        notifyDataSetChanged() //Source: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter
    }
}


