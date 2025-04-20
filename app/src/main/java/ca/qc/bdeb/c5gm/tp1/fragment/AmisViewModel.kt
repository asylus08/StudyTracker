package ca.qc.bdeb.c5gm.tp1.fragment

import GestionnaireSession
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qc.bdeb.c5gm.tp1.dao.UtilisateurDao
import ca.qc.bdeb.c5gm.tp1.entite.Utilisateur
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AmisViewModel : ViewModel() {
    val firestore = Firebase.firestore
    val utilisateurDao = UtilisateurDao.getInstance(firestore)
    val amisUtilisateur = MutableLiveData<MutableList<Utilisateur>>()

    // Méthode pour populer la liste d'amis
    fun populerAmis(session: GestionnaireSession, estClassement: Boolean) {
        var listeAmis = mutableListOf<Utilisateur>()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // On cherche l'utilisateur courant
                val utilisateur = utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())
                if (utilisateur != null) {

                    // On récupère ses amis
                    for (idAmi in utilisateur.amis) {
                        val ami = utilisateurDao.getUtilisateurParId(idAmi)
                        if (ami != null) {
                            listeAmis.add(ami)
                        }
                    }
                    // Si c'est pour un fin de classement, on ajoute l'utilisateur aussi
                    if (estClassement)
                        listeAmis.add(utilisateur)

                    withContext(Dispatchers.Main) {
                        amisUtilisateur.value = listeAmis // On le donne au mutable live data
                    }
                }
            }
        }
    }

    // Méthode pour ajouter un ami
    fun ajouterAmi(idAmi: String, session: GestionnaireSession) {
        viewModelScope.launch {
            val amiTrouve = utilisateurDao.getUtilisateurParId(idAmi)
            val utilisateur = utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())

            if (amiTrouve != null && utilisateur != null) {
                if (amiTrouve.idUtilisateurPK != utilisateur.idUtilisateurPK) {
                    utilisateur.let {
                        if (!it.amis.any { ami -> ami == amiTrouve.idUtilisateurPK }) {

                            val amisUtilisateurActuel = utilisateur.amis.toMutableList()
                            amisUtilisateurActuel.add(amiTrouve.idUtilisateurPK)
                            utilisateurDao.updateUtilisateur(utilisateur.copy(amis = amisUtilisateurActuel))

                            val amisAmiTrouve = amiTrouve.amis.toMutableList()
                            amisAmiTrouve.add(utilisateur.idUtilisateurPK)
                            utilisateurDao.updateUtilisateur(amiTrouve.copy(amis = amisAmiTrouve))

                            // Met à jour la liste des amis dans le LiveData
                            populerAmis(session, estClassement = false)
                        }
                    }
                }
            }
        }
    }

    // Méthode pour supprimer un ami
     fun supprimerAmi(utilisateur: Utilisateur, session: GestionnaireSession) {
         viewModelScope.launch {
             val utilisateurActuel =
                 utilisateurDao.getUtilisateurParId(session.retournerIdUtilisateur())

             utilisateurActuel?.let {
                 val amisActuels = it.amis.filter { ami -> ami != utilisateur.idUtilisateurPK }
                 utilisateurDao.updateUtilisateur(it.copy(amis = amisActuels))

                 val amiTrouve = utilisateurDao.getUtilisateurParId(utilisateur.idUtilisateurPK)
                 amiTrouve?.let { ami ->
                     val amisAmi = ami.amis.filter { amiUtilisateur -> amiUtilisateur != utilisateurActuel.idUtilisateurPK }
                     utilisateurDao.updateUtilisateur(ami.copy(amis = amisAmi))
                 }

                 // Met à jour la liste des amis dans le LiveData
                 populerAmis(session, estClassement = false)
             }
         }
     }
}