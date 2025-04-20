package ca.qc.bdeb.c5gm.tp1.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.entite.Utilisateur
import ca.qc.bdeb.c5gm.tp1.holder.ItemClassementHolder

class ListeClassementAdapter(
    private var amisUtilisateur: List<Utilisateur>?,
): RecyclerView.Adapter<ItemClassementHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemClassementHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liste_classement,
            parent, false)

        return ItemClassementHolder(view)
    }

    override fun onBindViewHolder(holder: ItemClassementHolder, position: Int) {
        val ami = amisUtilisateur?.get(position)

        if (ami != null) {
            val numeroClassement = position + 1
            holder.classement.text = numeroClassement.toString()
            holder.nomAmi.text = ami.nomUtilisateur
            holder.compteurStreak.text = ami.streak.toString()

        }
    }

    override fun getItemCount(): Int = amisUtilisateur!!.size

    // Méthode pour trier le classement par le streak
    fun trierClassement() {
        amisUtilisateur = amisUtilisateur?.sortedByDescending { it.streak }
        notifyDataSetChanged()
    }
}