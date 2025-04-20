// NavBarFragment.java
package ca.qc.bdeb.c5gm.tp1


import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class NavBarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_navigation, container, false)


        // Set up button click listeners
        view.findViewById<View>(R.id.button_home).setOnClickListener { v: View? -> }

        view.findViewById<View>(R.id.button_settings).setOnClickListener { v: View? -> }

        view.findViewById<View>(R.id.button_profile).setOnClickListener { v: View? -> }

        return view
    }
}