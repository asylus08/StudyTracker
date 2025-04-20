package ca.qc.bdeb.c5gm.tp1.dao

import android.util.Log
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class LesDevoirsDao private constructor(firestoreInstance: FirebaseFirestore) {

    val collectionDevoir = firestoreInstance.collection("Devoir")

    suspend fun insertDevoir(devoir: LesDevoirs) {
        val documentCreer = collectionDevoir
                                .add(devoir)
                                .await()

        val devoirAvecID = devoir.copy(idDevoirPK = documentCreer.id)
        collectionDevoir.document(documentCreer.id).set(devoirAvecID)
        Log.d("Firestore", "Devoir ajouté avec succès : ${devoirAvecID}")
    }

    suspend fun updateDevoir(devoir: LesDevoirs) {
        val devoirMap = devoir.toMap()
        collectionDevoir
            .document(devoir.idDevoirPK.toString())
            .update(devoirMap)
            .await()
    }

    suspend fun deleteDevoir(devoir: LesDevoirs) {
        collectionDevoir
            .document(devoir.idDevoirPK.toString())
            .delete()
            .await()
    }

    suspend fun getDevoirParId(id: String): LesDevoirs? {
        val resultat = collectionDevoir
            .whereEqualTo("idDevoirPK", id)
            .get()
            .await()

        return resultat.documents.firstOrNull()?.toObject(LesDevoirs::class.java)
    }

    suspend fun getDevoirsParIdUtilisateurs(idUtilisateur: String): List<LesDevoirs> {
        val resultat = collectionDevoir
            .whereEqualTo("idUtilisateurFK", idUtilisateur)
            .get()
            .await()

        Log.d("Firestore", "Documents trouvés pour idUtilisateur=$idUtilisateur : ${resultat.documents.size}")
        // Source: Prompt ChatGPT: How to search multiple documents in Firebase
        return resultat.documents.mapNotNull { it.toObject(LesDevoirs::class.java) }
    }

    companion object {
        @Volatile
        private var INSTANCE: LesDevoirsDao? = null

        fun getInstance(firestoreInstance: FirebaseFirestore): LesDevoirsDao {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: LesDevoirsDao(firestoreInstance)
                INSTANCE = instance
                instance
            }
        }
    }

}