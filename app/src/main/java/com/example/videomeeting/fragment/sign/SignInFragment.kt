package com.example.videomeeting.fragment.sign

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.videomeeting.R
import com.example.videomeeting.databinding.FragmentSignInBinding
import com.example.videomeeting.model.VideoMeetingViewModel
import com.example.videomeeting.model.VideoMeetingViewModelFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class SignInFragment : Fragment() {
    private val sharedViewModel: VideoMeetingViewModel by activityViewModels {
        VideoMeetingViewModelFactory(
            (activity?.application)
        )
    }
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var _emailText: String
    private val emailText get() = _emailText
    private lateinit var _passwordText: String
    private val passwordText get() = _passwordText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkEntryUser()
        binding.apply {
            textSignUp.setOnClickListener { goToSignUpFragment() }
            btnSignIn.setOnClickListener { signIn() }
        }
    }

    private fun checkEntryUser() {
        sharedViewModel.checkEntryUser(binding)
    }

    private fun goToSignUpFragment() {
        sharedViewModel.signInToSignUpFragment(binding)
    }


    private fun signIn() {
        sharedViewModel.showSignInProgress(binding)
        if (isEntryValid()) {
            sharedViewModel.getCurrentSignInUser(emailText, passwordText, binding)
        }
    }



    private fun isEntryValid(): Boolean {
        _emailText = binding.inputEmail.text.toString().trim() { it <= ' ' }
        _passwordText = binding.inputPassword.text.toString().trim() { it <= ' ' }
        if (emailText.isEmpty()) {
            binding.inputEmail.error = (getString(R.string.required))
            return false
        } else if (!sharedViewModel.validEmail(emailText)) {
            binding.inputEmail.error = (getString(R.string.enter_valid_email))
            return false
        } else {
            binding.inputEmail.error = (null)
        }
        if (passwordText.isEmpty()) {
            binding.inputPassword.error = (getString(R.string.required))
            return false
        } else if (!sharedViewModel.validPassword(passwordText)) {
            binding.inputPassword.error = (getString(R.string.enter_valid_password))
            return false
        } else {
            binding.inputPassword.error = (null)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}