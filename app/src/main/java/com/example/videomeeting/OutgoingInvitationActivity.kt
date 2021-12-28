package com.example.videomeeting

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeeting.databinding.ActivityOutgoingInvitationBinding
import com.example.videomeeting.model.UserWithToken
import com.example.videomeeting.model.VideoMeetingViewModel
import com.example.videomeeting.model.VideoMeetingViewModelFactory
import com.example.videomeeting.utilities.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class OutgoingInvitationActivity : AppCompatActivity() {
    private var user: UserWithToken? = null
    private var meetingType: String? = null
    private var _inviterToken: String? = null
    private val inviterToken get() = _inviterToken
    private var isMultiple: Boolean? = null
    private var multipleVideoType: String? = null
    private var _binding: ActivityOutgoingInvitationBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: VideoMeetingViewModel by viewModels {
        VideoMeetingViewModelFactory(
            (application)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOutgoingInvitationBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        user = navigationArgs.user
//        meetingType = navigationArgs.type


        user = intent.getSerializableExtra("user") as? UserWithToken
        meetingType = intent.getStringExtra(TYPE)

        multipleVideoType = intent.getStringExtra(TYPE)
        isMultiple = intent.getBooleanExtra(IS_MULTIPLE, false)

        bind()
        getInviterToken()
    }

    private fun bind() {
        if (meetingType != null && user != null) {
            binding.apply {
                textFirstChar.text = user!!.firstName.substring(0, 1)
                textUserName.text = String.format("%s %s", user!!.firstName, user!!.lastName)
                textEmail.text = user!!.email
            }
            if (meetingType.equals(VIDEO_TYPE)) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_video)
            } else {
                binding.imageMeetingType.setImageResource(R.drawable.ic_audio)
            }
        }
        binding.imageStopInvitation.setOnClickListener {
            if (user != null) {
                cancelInvitation()
            }
        }
    }

    private fun cancelInvitation() {
        sharedViewModel.cancelInvitation(
            user!!.token,
            this
        )
    }

    private fun initiateMeeting(meetingType: String, receiverToken: String,
                                receivers: ArrayList<UserWithToken>?) {
        sharedViewModel.initiateMeeting(
            meetingType, receiverToken, inviterToken.toString(),
            receivers,
            binding,
            this
        )
    }

    private fun getInviterToken() {
        lifecycle.coroutineScope.launch {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                _inviterToken = task.result.toString()
                if (meetingType != null) {
                    if (isMultiple == true){
                        val type = object : TypeToken<ArrayList<UserWithToken>>() {}.type
                        val receivers: ArrayList<UserWithToken> =
                            Gson().fromJson(intent.getStringExtra(SELECTED_USERS), type)
                        initiateMeeting(meetingType!!, null.toString(), receivers)
                    }else{
                        if (user != null){
                            initiateMeeting(meetingType!!, user!!.token, null)
                        }
                    }
                }
            })
        }
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

