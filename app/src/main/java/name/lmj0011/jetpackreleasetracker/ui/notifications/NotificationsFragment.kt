package name.lmj0011.jetpackreleasetracker.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import name.lmj0011.jetpackreleasetracker.R

class NotificationsFragment : Fragment() {

    //private lateinit var binding: FragmentNotificationsBinding
    private val notificationsViewModel by viewModels<NotificationsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
        //    binding.textNotifications.text = it
        //})
    }
}
