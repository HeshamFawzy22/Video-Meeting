package com.example.videomeeting.firebase

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.videomeeting.IncomingInvitationActivity
import com.example.videomeeting.utilities.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val type = remoteMessage.data[REMOTE_MSG_TYPE]
        if (type != null) {
            if (type == REMOTE_MSG_INVITATION) {
                val intent = Intent(applicationContext, IncomingInvitationActivity::class.java)
                intent.putExtra(
                    REMOTE_MSG_MEETING_TYPE,
                    remoteMessage.data[REMOTE_MSG_MEETING_TYPE]
                ).putExtra(KEY_FIRST_NAME, remoteMessage.data[KEY_FIRST_NAME])
                    .putExtra(KEY_LAST_NAME, remoteMessage.data[KEY_LAST_NAME])
                    .putExtra(KEY_EMAIL, remoteMessage.data[KEY_EMAIL])
                    .putExtra(
                        REMOTE_MSG_INVITER_TOKEN,
                        remoteMessage.data[KEY_FCM_TOKEN]
                    ).putExtra(REMOTE_MSG_MEETING_ROOM, remoteMessage.data[REMOTE_MSG_MEETING_ROOM])
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            } else if (type == REMOTE_MSG_INVITATION_RESPONSE) {
                val intent = Intent(REMOTE_MSG_INVITATION_RESPONSE)
                intent.putExtra(
                    REMOTE_MSG_INVITATION_RESPONSE,
                    remoteMessage.data[REMOTE_MSG_INVITATION_RESPONSE]
                )
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
            }
        }
    }
}