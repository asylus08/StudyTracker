package ca.qc.bdeb.c5gm.tp1.fragment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qc.bdeb.c5gm.tp1.dao.LesDevoirsDao
import ca.qc.bdeb.c5gm.tp1.entite.LesDevoirs
import kotlinx.coroutines.launch

class DevoirViewModel : ViewModel() {
    private val _listeAFaire = MutableLiveData<MutableList<LesDevoirs>>()
    val listeAFaire: LiveData<MutableList<LesDevoirs>> get() = _listeAFaire
    private val _listeCompletee = MutableLiveData<MutableList<LesDevoirs>>()
    val listeCompletee: LiveData<MutableList<LesDevoirs>> get() = _listeCompletee

    fun chargerDevoirs(dao: LesDevoirsDao, userId: String) {
        Log.d("ViewModel", "Chargement des devoirs pour l'utilisateur : $userId")
        viewModelScope.launch {
            val devoirs = dao.getDevoirsParIdUtilisateurs(userId)
            Log.d("ViewModel", "Devoirs récupérés : ${devoirs.size}")
            _listeAFaire.postValue(devoirs.filter { !it.estComplete }.toMutableList())
            _listeCompletee.postValue(devoirs.filter { it.estComplete }.toMutableList())
        }
    }

    fun ajouterDevoir(devoir: LesDevoirs) {
        val listeActuelle = _listeAFaire.value ?: mutableListOf()
        listeActuelle.add(devoir)
        _listeAFaire.postValue(listeActuelle)
    }

}
