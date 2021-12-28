package com.example.videomeeting.fragment.sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import com.example.videomeeting.R
import com.example.videomeeting.databinding.FragmentSignUpBinding
import com.example.videomeeting.model.User
import com.example.videomeeting.model.VideoMeetingViewModel
import com.example.videomeeting.model.VideoMeetingViewModelFactory
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val sharedViewModel: VideoMeetingViewModel by activityViewModels {
        VideoMeetingViewModelFactory(
            (activity?.application)
        )
    }
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var _userFirstNameText: String
    private val userFirstNameText get() = _userFirstNameText
    private lateinit var _userLastNameText: String
    private val userLastNameText get() = _userLastNameText
    private lateinit var _emailText: String
    private val emailText get() = _emailText
    private lateinit var _passwordText: String
    private val passwordText get() = _passwordText
    private lateinit var _confirmPasswordText: String
    private val confirmPasswordText get() = _confirmPasswordText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnBack.setOnClickListener { exit() }
            textSignIn.setOnClickListener { exit() }
            btnSignUp.setOnClickListener { signUp() }
        }

    }

    private fun signUp() {
        sharedViewModel.showSignUpProgress(binding)
        lifecycle.coroutineScope.launch {
            if (isEntryValid()) {
                addUserToFirebase(
                    userFirstNameText,
                    userLastNameText,
                    emailText,
                    passwordText
                )
            }
        }
    }

    private fun isEntryValid(): Boolean {
        _userFirstNameText = binding.inputFirstName.text.toString().trim { it <= ' ' }
        _userLastNameText = binding.inputLastName.text.toString().trim { it <= ' ' }
        _emailText = binding.inputEmail.text.toString().trim { it <= ' ' }
        _passwordText = binding.inputPassword.text.toString().trim { it <= ' ' }
        _confirmPasswordText =
            binding.inputConfirmPassword.text.toString().trim()
        if (userFirstNameText.isEmpty()) {
            binding.inputFirstName.error = (getString(R.string.required))
            return false
        } else {
            binding.inputFirstName.error = null
        }
        if (userLastNameText.isEmpty()) {
            binding.inputLastName.error = (getString(R.string.required))
            return false
        } else {
            binding.inputLastName.error = null
        }
        if (emailText.isEmpty()) {
            binding.inputEmail.error = getString(R.string.required)
            return false
        } else if (!sharedViewModel.validEmail(binding.inputEmail.text.toString().trim())) {
            binding.inputEmail.error = getString(R.string.errorEmail)
            return false
        } else {
            binding.inputEmail.error = null
        }
        if (passwordText.isEmpty()) {
            binding.inputPassword.error = getString(R.string.required)
            return false
        } else if (!sharedViewModel.validPassword(binding.inputPassword.text.toString().trim())) {
            binding.inputPassword.error = getString(R.string.errorPassword)
            return false
        } else {
            binding.inputPassword.error = null
        }
        when {
            confirmPasswordText.isEmpty() -> {
                binding.inputConfirmPassword.error = getString(R.string.required)
                return false
            }
            confirmPasswordText != passwordText -> {
                binding.inputPassword.error = getString(R.string.notMatchingPasswords)
                binding.inputConfirmPassword.error = getString(R.string.notMatchingPasswords)
                return false
            }
            else -> {
                binding.inputPassword.error = null
                binding.inputConfirmPassword.error = null
            }
        }
        return true
    }

    private fun exit() {
        activity?.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addUserToFirebase(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ) {
        val user = User(firstName, lastName, email, password)
        sharedViewModel.addUserToFirebase(
            binding,
            user,
        )
    }


}