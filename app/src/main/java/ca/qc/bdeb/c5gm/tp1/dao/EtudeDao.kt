package ca.qc.bdeb.c5gm.tp1.dao

import ca.qc.bdeb.c5gm.tp1.entite.Etude
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class EtudeDao private constructor(firestoreInstance: FirebaseFirestore){

    val collectionEtude = firestoreInstance.collection("Etude")

    suspend fun insertEtude(etude: Etude) {
        val documentCreer = collectionEtude
                                .add(etude)
                                .await()
        val etudeAvecID = etude.copy(idEtudePK = documentCreer.id)
        collectionEtude.document(documentCreer.id).set(etudeAvecID)
    }

    suspend fun deleteEtude(etude: Etude) {
        collectionEtude
            .document(etude.idEtudePK.toString())
            .delete()
            .await()
    }

    suspend fun getEtudesParIdUtilisateuer(idUtilisateur: String): MutableList<Etude>{
        val resultat = collectionEtude
            .whereEqualTo("idUtilisateurFK", idUtilisateur)
            .get()
            .await()

        return resultat.documents.mapNotNull { it.toObject(Etude::class.java) }
            .toMutableList()
    }

    companion object {
        @Volatile
        private var INSTANCE: EtudeDao? = null

        fun getInstance(firestoreInstance: FirebaseFirestore): EtudeDao {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: EtudeDao(firestoreInstance)
                INSTANCE = instance
                instance
            }
        }
    }
}
