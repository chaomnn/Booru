package baka.chaomian.booru.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import baka.chaomian.booru.R
import baka.chaomian.booru.databinding.FragmentLoginBinding
import baka.chaomian.booru.utils.LoginManager

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        binding.login.setOnClickListener {
            val username = binding.username.editText!!.text.toString()
            val password = binding.password.editText!!.text.toString()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                LoginManager.username = username
                LoginManager.apiKey = password
                LoginManager.isUserLoggedIn = true
                (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }
}
