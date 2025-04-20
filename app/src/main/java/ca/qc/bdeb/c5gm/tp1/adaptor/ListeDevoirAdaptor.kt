package ca.qc.bdeb.c5gm.tp1.adaptor

import android.content.Context
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ca.qc.bdeb.c5gm.tp1.R
import ca.qc.bdeb.c5gm.tp1.activite.Devoirs
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import ca.qc.bdeb.c5gm.tp1.holder.ItemDevoirsHolder

class ListeDevoirsAdaptor(
    private val context: Context,
    private var devoirs: MutableList<LesDevoirs>,
    private val completerDevoir: (LesDevoirs) -> Unit,
    private val modifierDevoir: (LesDevoirs) -> Unit
) : RecyclerView.Adapter<ItemDevoirsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemDevoirsHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.liste_devoirs, parent, false)
        return ItemDevoirsHolder(view)
    }

    override fun getItemCount(): Int = devoirs.size

    override fun onBindViewHolder(holder: ItemDevoirsHolder, position: Int) {
        val devoirActuel = devoirs[position]

        // Remplir les vues de chaque élément
        holder.nomDevoir.text = devoirActuel.nomDevoir
        holder.description.text = devoirActuel.description
        holder.indicePriorite.text = devoirActuel.indicePriorite

        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.itemBackgroundColor, typedValue, true)
        holder.layout.setBackgroundColor(typedValue.data)

        // Vérifier si le mode sombre est activé
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // Appliquer la couleur de texte noire pour le mode sombre uniquement
            holder.btnModifier.setTextColor(ContextCompat.getColor(context, R.color.noir))
            holder.btnSupprimer.setTextColor(ContextCompat.getColor(context, R.color.noir))
        } else {
            // Laisser la couleur de texte par défaut en mode clair
            holder.btnModifier.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light))
            holder.btnSupprimer.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light))
        }

        // Charger l'image à partir de l'URI si disponible
        if (!devoirActuel.imageUri.isNullOrEmpty()) {
            val uri = Uri.parse(devoirActuel.imageUri)
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    holder.imageDevoir.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                holder.imageDevoir.setImageResource(R.drawable.baseline_image_24)
            }
        } else {
            holder.imageDevoir.setImageResource(R.drawable.baseline_image_24)
        }

        // Gestion de la complétion du devoir avec le checkbox
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = devoirActuel.estComplete

        holder.checkBox.setOnCheckedChangeListener { _, estCoche ->
            devoirActuel.estComplete = estCoche
            completerDevoir(devoirActuel)
        }

        // Bouton pour supprimer le devoir
        holder.btnSupprimer.setOnClickListener {
            (context as Devoirs).confirmerSuppressionDevoir(devoirActuel)
        }


        // Bouton pour modifier le devoir
        holder.btnModifier.setOnClickListener {
            modifierDevoir(devoirActuel)
        }
    }

    // Méthode pour mettre à jour la liste des devoirs
    fun mettreAJourDevoirs(nouveauxDevoirs: List<LesDevoirs>) {
        devoirs = nouveauxDevoirs.toMutableList()
        notifyDataSetChanged()
    }
}