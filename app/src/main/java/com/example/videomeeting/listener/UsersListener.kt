package com.example.videomeeting.listener

import com.example.videomeeting.model.UserWithToken

interface UsersListener {
    fun initiateVideoMeeting(user: UserWithToken)
    fun initiateAudioMeeting(user: UserWithToken)
    fun onMultipleUserAction(isMultipleUsersSelected: Boolean)
}