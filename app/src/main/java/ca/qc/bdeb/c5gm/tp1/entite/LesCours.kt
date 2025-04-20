package ca.qc.bdeb.c5gm.tp1.entite

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class LesCours(
    @PrimaryKey(autoGenerate = true)
    val idCoursPK: String = "",
    val idUtilisateurFK: String = "",
    val nomCours: String = "",
) : Parcelable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf("idUtilisateurFK" to idUtilisateurFK, "nomCours" to nomCours)
    }
}