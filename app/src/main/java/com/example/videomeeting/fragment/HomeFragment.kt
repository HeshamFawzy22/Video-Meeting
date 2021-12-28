package com.example.videomeeting.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.videomeeting.adapter.UsersAdapter
import com.example.videomeeting.databinding.FragmentHomeBinding
import com.example.videomeeting.listener.UsersListener
import com.example.videomeeting.model.UserWithToken
import com.example.videomeeting.model.VideoMeetingViewModel
import com.example.videomeeting.model.VideoMeetingViewModelFactory
import com.example.videomeeting.utilities.KEY_FIRST_NAME
import com.example.videomeeting.utilities.KEY_LAST_NAME
import com.example.videomeeting.utilities.PreferenceManager
import java.util.*

class HomeFragment : Fragment(), UsersListener {
    private val sharedViewModel: VideoMeetingViewModel by activityViewModels {
        VideoMeetingViewModelFactory(
            (activity?.application)
        )
    }

    private var _preferenceManager: PreferenceManager? = null
    private val preferenceManager get() = _preferenceManager!!
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _preferenceManager = PreferenceManager(requireContext())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind()
        createNewToken()
        loadUsers()
    }

    private fun loadUsers() {
        try {
            sharedViewModel.loadUsers(binding).observe(viewLifecycleOwner, { users ->
                if (users.isNotEmpty()) {
                    initRecyclerView(users)
                } else {
                    sharedViewModel.showTextErrorMessage(binding)
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initRecyclerView(users: List<UserWithToken>?) {
        try {
            binding.apply {
                usersRecyclerView.adapter = UsersAdapter(users!!, this@HomeFragment)
                usersRecyclerView.hasFixedSize()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun createNewToken() {
        sharedViewModel.getNewToken()
    }

    private fun signOut() {
        sharedViewModel.signOut(binding)
    }

    private fun bind() {
        binding.apply {
            textTitle.text = String.format(
                "%s %s",
                preferenceManager.getString(KEY_FIRST_NAME),
                preferenceManager.getString(KEY_LAST_NAME)
            )
            textSignOut.setOnClickListener { signOut() }
            swipeRefreshLayout.setOnRefreshListener { loadUsers() }
        }
    }

    override fun initiateVideoMeeting(user: UserWithToken) {
        sharedViewModel.initiateVideoMeeting(user, binding)
    }

    override fun initiateAudioMeeting(user: UserWithToken) {
        sharedViewModel.initiateAudioMeeting(user, binding)
    }

    override fun onMultipleUserAction(isMultipleUsersSelected: Boolean) {
        sharedViewModel.onMultipleUserAction(isMultipleUsersSelected,binding)

    }

}