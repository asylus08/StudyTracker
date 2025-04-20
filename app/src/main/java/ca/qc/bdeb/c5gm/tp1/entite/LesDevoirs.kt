package ca.qc.bdeb.c5gm.tp1.entite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class LesDevoirs(
    @PrimaryKey(autoGenerate = true)
    val idDevoirPK: String? = null,
    val idUtilisateurFK: String = "",
    var estComplete: Boolean = false,
    val imgDevoir: Int = 0,
    val nomDevoir: String = "",
    val description: String = "",
    val imageUri: String? = null,
    var indicePriorite: String = "Normale"
) : Parcelable {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "idUtilisateurFK" to idUtilisateurFK,
            "estComplete" to estComplete,
            "imgDevoir" to imgDevoir,
            "nomDevoir" to nomDevoir,
            "description" to description,
            "imageUri" to imageUri,
            "indicePriorite" to indicePriorite
        )
    }
}
