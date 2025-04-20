package ca.qc.bdeb.c5gm.tp1.dao

import ca.qc.bdeb.c5gm.tp1.entite.LesCours
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LesCoursDao private constructor(firestoreInstance: FirebaseFirestore) {

    val collectionCours = firestoreInstance.collection("LesCours")


    suspend fun insertCours(cours: LesCours) {
        val documentCreer = collectionCours
                                .add(cours)
                                .await()
        val coursAvecID = cours.copy(idCoursPK = documentCreer.id)
        collectionCours.document(documentCreer.id).set(coursAvecID)
    }

    suspend fun updateCours(cours: LesCours) {
        val coursMap = cours.toMap()

        collectionCours
            .document(cours.idCoursPK.toString())
            .update(coursMap)
            .await()
    }

    suspend fun deleteCours(cours: LesCours) {
        collectionCours
            .document(cours.idCoursPK.toString())
            .delete()
            .await()

    }

    suspend fun getCoursParIdUtilisateur(IdUtilisateur: String): MutableList<LesCours> {
        val resultat = collectionCours
            .whereEqualTo("idUtilisateurFK", IdUtilisateur)
            .get()
            .await()

        return resultat.documents.mapNotNull { it.toObject(LesCours::class.java) }
            .toMutableList()
    }


    companion object {
        @Volatile
        private var INSTANCE: LesCoursDao? = null

        fun getInstance(firestoreInstance: FirebaseFirestore): LesCoursDao {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: LesCoursDao(firestoreInstance)
                INSTANCE = instance
                instance
            }
        }
    }
}
