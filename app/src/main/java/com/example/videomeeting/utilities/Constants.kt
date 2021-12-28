@file:JvmName("Constants")

package com.example.videomeeting.utilities

const val KEY_COLLECTION_USERS = "USERS"
const val KEY_FIRST_NAME = "firstName"
const val KEY_LAST_NAME = "lastName"
const val KEY_EMAIL = "email"
const val KEY_PASSWORD = "password"
const val KEY_USER_ID = "userId"
const val KEY_PREFERENCE_NAME = "videoMeetingPreference"
const val KEY_IS_SIGNED_IN = "isSignedIn"
const val KEY_FCM_TOKEN = "fcmToken"
const val BASE_URL = "https://fcm.googleapis.com/fcm/"
const val VIDEO_TYPE = "video"
const val AUDIO_TYPE = "audio"
const val TYPE = "type"
const val IS_MULTIPLE = "isMultiple"
const val SELECTED_USERS = "selectedUsers"
const val REMOTE_MSG_AUTHORIZATION = "Authorization"
const val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
const val REMOTE_MSG_TYPE = "type"
const val REMOTE_MSG_INVITATION = "invitation"
const val REMOTE_MSG_MEETING_TYPE = "meetingType"
const val REMOTE_MSG_INVITER_TOKEN = "inviterToken"
const val REMOTE_MSG_DATA = "data"
const val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"
const val REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse"
const val REMOTE_MSG_INVITATION_ACCEPTED = "accepted"
const val REMOTE_MSG_INVITATION_REJECTED = "rejected"
const val REMOTE_MSG_INVITATION_CANCELLED = "cancelled"
const val REMOTE_MSG_MEETING_ROOM = "meetingRoom"

fun getRemoteMsgHeaders(): HashMap<String, String> {
    val headers: HashMap<String, String> = HashMap()
    headers[REMOTE_MSG_AUTHORIZATION] =
        "key=AAAAGGoc3nY:APA91bGqsK0mzfDc5N20g6WjEnI0emhKEquaeKTQVTP6IK_9mohDe31r3Q9rKI7mh1c3ImMUAyybm4I7XKRmNwk0mJ19s5n5nmm91EfjnoWSNVufILBUS912xLe12vvQIukwz-N0Af2T"
    headers[REMOTE_MSG_CONTENT_TYPE] = "application/json"
    return headers
}

