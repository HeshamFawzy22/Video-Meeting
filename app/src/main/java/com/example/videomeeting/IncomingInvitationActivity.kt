package com.example.videomeeting

import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeeting.databinding.ActivityIncomingInvitationBinding
import com.example.videomeeting.model.VideoMeetingViewModel
import com.example.videomeeting.model.VideoMeetingViewModelFactory
import com.example.videomeeting.utilities.*

class IncomingInvitationActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityIncomingInvitationBinding
    private val binding get() = _binding
    private val sharedViewModel: VideoMeetingViewModel by viewModels {
        VideoMeetingViewModelFactory(
            (application)
        )
    }

    //    private val _firstName : MutableLiveData<String>? = null
//    private var firstName = liveData<String> { _firstName }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityIncomingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()
    }


    private fun bind() {
        val meetingType = intent.getStringExtra(REMOTE_MSG_MEETING_TYPE)
        val firstName = intent.getStringExtra(KEY_FIRST_NAME)
        val lastName = intent.getStringExtra(KEY_LAST_NAME)
        val email = intent.getStringExtra(KEY_EMAIL)
        val inviterToken = intent.getStringExtra(REMOTE_MSG_INVITER_TOKEN)
        binding.imageAcceptInvitation.setOnClickListener {
            sendInvitationResponse(REMOTE_MSG_INVITATION_ACCEPTED, inviterToken)
        }
        binding.imageRejectInvitation.setOnClickListener {
            sendInvitationResponse(REMOTE_MSG_INVITATION_REJECTED, inviterToken)
        }
        if (meetingType != null && firstName != null) {
            binding.apply {
                textFirstChar.text = firstName.substring(0, 1)
                textUserName.text = String.format("%s %s", firstName, lastName)
                textEmail.text = email
            }
            if (meetingType == VIDEO_TYPE) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_video)
            }else{
                binding.imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }
    }

    private fun sendInvitationResponse(invitationResponse: String, inviterToken: String?) {
        sharedViewModel.sendInvitationResponse(
            invitationResponse,
            inviterToken.toString(),
            this@IncomingInvitationActivity
        )
    }


    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            sharedViewModel.onReceiveResponse(this),
            IntentFilter(REMOTE_MSG_INVITATION_RESPONSE)
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(
            sharedViewModel.onReceiveResponse(this)
        )
    }
}