import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Source: https://www.digitalocean.com/community/tutorials/android-sharedpreferences-kotlin
// Source: https://medium.com/@ZahraHeydari/singleton-pattern-in-kotlin-b09380c53b14
class GestionnaireSession private constructor(context: Context) {

    // Déclaration d'un fichier shared preferences
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile
        private var instance: GestionnaireSession? = null

        // Implémentation d'un patron de singleton
        fun getInstance(context: Context): GestionnaireSession {
            return instance ?: synchronized(this) {
                instance ?: GestionnaireSession(context).also { instance = it }
            }
        }
    }

    // Méthode pour sauvegarder l'id d'un utilisateur et la préférences de thème dans le fichier
    fun connexionUtilisateur(idUtilisateur: String, modeSombre: Boolean, streak: Int, aContinuerStreak: String?) {
        with(sharedPreferences.edit()) {
            putString("idUtilisateur", idUtilisateur)
            putBoolean("modeSombre", modeSombre)
            putInt("streak", streak)
            putString("aContinuerStreak", aContinuerStreak)
            apply()
        }
    }

    // Méthode pour modifier la préférence de thème
    fun modifierTheme(modeSombre: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean("modeSombre", modeSombre)
            apply()
        }
    }

    // Méthode pour modifier la date du streak courant
    fun modifierDateStreakCourant(date: String) {
        with(sharedPreferences.edit()) {
            putString("aContinuerStreak", date)
            apply()
        }
    }

    // Méthode pour incrémenter le streak
    fun continueStreakCourant() {
        val streakCourant = sharedPreferences.getInt("streak", 0)

        with(sharedPreferences.edit()) {
            putInt("streak", streakCourant + 1)
            apply()
        }
    }

    // Méthode pour retourner le streak courant
    fun retournerStreakCourant() : Int {
        return sharedPreferences.getInt("streak", 0)
    }

    // Méthode pour verifier si l'utilisateur à continuer son streak
    fun retournerAContinuerStreak() : Boolean {
        val dateCourant = LocalDate.now()
        val dateCourantFormatter = dateCourant.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val dateSauvegarder = sharedPreferences.getString("aContinuerStreak", "")

        return dateSauvegarder == dateCourantFormatter
    }

    // Méthode pour retourner l'id de l'utilisateur connecter
    fun retournerIdUtilisateur(): String {
        return sharedPreferences.getString("idUtilisateur", "").toString()
    }

    // Méthode pour retourner la préférence de thème de l'utilisateur connecter
    fun retournerModeSombre(): Boolean {
        return sharedPreferences.getBoolean("modeSombre", false)
    }

    // Méthode pour déconnecter l'utilisateur
    fun deconnexion(context: Context) {
        // On tue les notification si on se déconnecte
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()

        with(sharedPreferences.edit()) {
            remove("idUtilisateur")
            remove("modeSombre")
            remove("aContinuerStreak")
            remove("streak")
            apply()
        }
    }
}
