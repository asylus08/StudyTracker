package ca.qc.bdeb.c5gm.tp1.entite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Etude(
    @PrimaryKey(autoGenerate = true)
    val idEtudePK: String? = null,
    val idUtilisateurFK: String = "",
    val nomSessionEtude: String? = null,
    val nomCours: String = "",
    val tempsEtude: Long = 0,
    val tacheCompleter: Int = 0,
    val tacheCreer: Int = 0
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf("idUtilisateurFK" to idUtilisateurFK, "nomSessionEtude" to nomSessionEtude,
            "nomCours" to nomCours, "tempsEtude" to tempsEtude, "tacheCompleter" to tacheCompleter,
            "tacheCreer" to tacheCreer)
    }
}
