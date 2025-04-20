package ca.qc.bdeb.c5gm.tp1.entite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Utilisateur(
    @PrimaryKey(autoGenerate = true)
    var idUtilisateurPK: String = "",
    val nomUtilisateur: String? = null,
    val motDePasse: String? = null,
    val modeSombre: Boolean = false,
    var streak: Int = 0,
    val dernierDateStreak: String? = null,
    val amis: List<String> = emptyList()
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nomUtilisateur" to nomUtilisateur,
            "motDePasse" to motDePasse,
            "modeSombre" to modeSombre,
            "streak" to streak,
            "dernierDateStreak" to dernierDateStreak,
            "amis" to amis
        )
    }
}
