package ca.qc.bdeb.c5gm.tp1.adaptor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.entite.Etude
import ca.qc.bdeb.c5gm.tp1.holder.ItemsSessionHolder

class ListeSessionAdaptor(
    private var lesSessionEtude: List<Etude>, // Liste contenant les sessions d'étude
    private var supprimerSession: (Etude) -> Unit // Méthode pour supprimer une session
) : RecyclerView.Adapter<ItemsSessionHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemsSessionHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.liste_session_etude, parent, false)
        return ItemsSessionHolder(view)
    }
    override fun onBindViewHolder(holder: ItemsSessionHolder, position: Int) {
        val session = lesSessionEtude[position]

        // Formattage du temps en format 00:00:00 (Fournie par ChatGPT)
        val heures = (session.tempsEtude / 3600).toInt()
        val minutes = ((session.tempsEtude % 3600) / 60).toInt()
        val secondes = (session.tempsEtude % 60).toInt()

        val tempFormater = String.format("%02d:%02d:%02d", heures, minutes, secondes)

        holder.nomSession.text = session.nomSessionEtude
        holder.temps.text = tempFormater
        holder.tacheCompleter.text = session.tacheCompleter.toString()
        holder.tacheCreer.text = session.tacheCreer.toString()
        holder.nomCours.text = session.nomCours

        holder.btnSupprimer.setOnClickListener { supprimerSession(session) }

    }

    override fun getItemCount(): Int = lesSessionEtude.size

    // Méthode pour mettre à jour les données du recycler view
    fun mettreAJourDonnee(LesSessionAJour: List<Etude>) {
        lesSessionEtude = LesSessionAJour
        notifyDataSetChanged()
    }
}