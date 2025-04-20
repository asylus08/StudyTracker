package ca.qc.bdeb.c5gm.tp1.dao

import ca.qc.bdeb.c5gm.tp1.entite.Utilisateur
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Source: https://firebase.google.com/docs/firestore/query-data/get-data?_gl=1*1hk26o6*_up*MQ..*_ga*MTE0ODgwMDgwOS4xNzMyMDQyMjQ3*_ga_CW55HF8NVT*MTczMjA0MjI0Ny4xLjAuMTczMjA0MjI0Ny4wLjAuMA..#kotlin+ktx_7
// Les commentaires suivants s'applique aux autres DAO
class UtilisateurDao private constructor(firestoreInstance: FirebaseFirestore) {

    // On récupére la collection
    private val collectionUtilisateur = firestoreInstance.collection("Utilisateur")

    // Méthode pour insérer un objet
    suspend fun insertUtilisateur(utilisateur: Utilisateur) {
        // on ajoute dans la collection
        val documentCreer = collectionUtilisateur
                                .add(utilisateur)
                                .await()

        // Dans notre cas, on va donner l'id du document a un champ pour facilter la lecture de donnée
        // on crée une copie avec l'id populer
        val utilisateurAvecID = utilisateur.copy(idUtilisateurPK = documentCreer.id)

        // On le met à jour
        collectionUtilisateur.document(documentCreer.id).set(utilisateurAvecID)
    }

    // Méthode pour mettre à jour un objet
    suspend fun updateUtilisateur(utilisateur: Utilisateur) {
        // Convertie l'utilisateur à un map
        val utilisateurMap = utilisateur.toMap()

        // On met à jour l'utilisateur
        collectionUtilisateur
            .document(utilisateur.idUtilisateurPK) // On spécifie l'id ici
            .update(utilisateurMap)
            .await()
    }

    // Méthode pour supprimer un objet
    suspend fun deleteUtilisateur(utilisateur: Utilisateur) {
        // Même principe que le update
        collectionUtilisateur
            .document(utilisateur.idUtilisateurPK)
            .delete()
            .await()
    }

    // Méthode pour receuillir un objet selon un champ spécifique
    suspend fun getUtilisateurParNomUtilisateur(nomUtilisateur: String): Utilisateur? {
        val resultat = collectionUtilisateur
            .whereEqualTo("nomUtilisateur", nomUtilisateur) // On veut le nom utilisateur
            .get()
            .await()

        // Source: Prompt ChatGPT: How to convert DocumentSnapshot to object
        return resultat.documents.firstOrNull()?.toObject(Utilisateur::class.java)
    }

    // Méthode pour vérifier les informations de l'utilisateur et le retourner
    suspend fun getUtilisateurParInfo(nomUtilisateur: String, motDePasse: String): Utilisateur? {
        val resultat = collectionUtilisateur
            .whereEqualTo("nomUtilisateur", nomUtilisateur)
            .whereEqualTo("motDePasse", motDePasse)
            .get()
            .await()

        return resultat.documents.firstOrNull()?.toObject(Utilisateur::class.java)
    }

    // Méthode pour retourner un utilisateur selon un identifiant
    suspend fun getUtilisateurParId(idUtilisateur: String): Utilisateur? {
        val resultat = collectionUtilisateur
            .whereEqualTo("idUtilisateurPK", idUtilisateur)
            .get()
            .await()

        return resultat.documents.firstOrNull()?.toObject(Utilisateur::class.java)
    }

    // On le transforme en singleton
    companion object {
        @Volatile
        private var INSTANCE: UtilisateurDao? = null

        fun getInstance(firestoreInstance: FirebaseFirestore): UtilisateurDao {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: UtilisateurDao(firestoreInstance)
                INSTANCE = instance
                instance
            }
        }
    }
}