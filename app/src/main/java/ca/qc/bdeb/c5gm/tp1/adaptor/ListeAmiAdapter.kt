package ca.qc.bdeb.c5gm.tp1.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.entite.Utilisateur
import ca.qc.bdeb.c5gm.tp1.holder.ItemAmiHolder

class ListeAmiAdapter(
    private var listeAmis: MutableList<Utilisateur>,
    private val supprimerAmi: (Utilisateur) -> Unit,
) : RecyclerView.Adapter<ItemAmiHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemAmiHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liste_amis, parent, false)
        return ItemAmiHolder(view)
    }

    override fun getItemCount(): Int = listeAmis.size

    override fun onBindViewHolder(holder: ItemAmiHolder, position: Int) {
        val ami = listeAmis[position]

        holder.nomAmi.text = ami.nomUtilisateur
        holder.btnSupprimer.setOnClickListener {
            supprimerAmi(ami)
        }
    }

    // Méthode pour mettre à jour la liste des amis
    fun mettreAJourListe() {
        notifyDataSetChanged()
    }
}