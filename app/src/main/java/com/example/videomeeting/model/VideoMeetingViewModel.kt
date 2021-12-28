package com.example.videomeeting.model

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import androidx.navigation.Navigation.findNavController
import com.example.videomeeting.OutgoingInvitationActivity
import com.example.videomeeting.R
import com.example.videomeeting.adapter.UsersAdapter
import com.example.videomeeting.databinding.*
import com.example.videomeeting.firebase.database.MyDatabase
import com.example.videomeeting.network.ClientApi
import com.example.videomeeting.utilities.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class VideoMeetingViewModel(private val application: Application?) : ViewModel(){
    private val _users = MutableLiveData<List<UserWithToken>>()
    private val users: LiveData<List<UserWithToken>> = _users
    private val _listOfUsers = ArrayList<UserWithToken>()
    private var userReference: CollectionReference = MyDatabase().userReference()
    private var _preferenceManager: PreferenceManager? = null
    private val preferenceManager get() = _preferenceManager!!
    private var _meetingRoom = String()
    private val meetingRoom = _meetingRoom

    init {
        _preferenceManager = PreferenceManager(application!!.applicationContext)
    }

    fun validEmail(emailText: String): Boolean {
        return emailText.contains("@") && emailText.contains(".")
    }

    fun validPassword(passwordText: String): Boolean {
        return passwordText.length >= 6
    }

    fun sendSignUpUserDataToSharedPreference(
        documentReference: DocumentReference,
        user: User
    ) {
        viewModelScope.launch {
            preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
            preferenceManager.putString(KEY_USER_ID, documentReference.id)
            preferenceManager.putString(KEY_FIRST_NAME, user.firstName)
            preferenceManager.putString(KEY_LAST_NAME, user.lastName)
            preferenceManager.putString(KEY_EMAIL, user.email)
            preferenceManager.putString(KEY_PASSWORD, user.password)
        }
    }

    fun addUserToFirebase(
        binding: FragmentSignUpBinding,
        user: User
    ) {
        viewModelScope.launch {
            userReference.add(user)
                .addOnSuccessListener { documentReference ->
                    sendSignUpUserDataToSharedPreference(
                        documentReference,
                        user
                    )
                    signUpToHomeFragment(binding)
                }.addOnFailureListener { e ->
                    hideSignUpProgress(binding)
                    Toast.makeText(application, e.message, Toast.LENGTH_LONG).show()
                }
        }
    }

    fun getCurrentSignInUser(
        emailText: String,
        passwordText: String,
        binding: FragmentSignInBinding
    ) {
        viewModelScope.launch {
            userReference.whereEqualTo(KEY_EMAIL, emailText)
                .whereEqualTo(KEY_PASSWORD, passwordText)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                        val user: DocumentSnapshot = task.result!!.documents[0]
                        sendSignInUserDataToSharedPreference(user)
                        signInToHomeFragment(binding)
                    }
                }.addOnFailureListener {
                    hideSignInProgress(binding)
                    Toast.makeText(
                        application,
                        application?.getString(R.string.unable_to_sign_in),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun signInToHomeFragment(binding: FragmentSignInBinding) {
        findNavController(binding.root)
            .navigate(R.id.action_signInFragment_to_homeFragment)
    }

    fun signInToSignUpFragment(binding: FragmentSignInBinding) {
        findNavController(binding.root)
            .navigate(R.id.action_signInFragment_to_signUpFragment)
    }

    private fun sendSignInUserDataToSharedPreference(user: DocumentSnapshot) {
        viewModelScope.launch {
            preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
            preferenceManager.putString(KEY_USER_ID, user.id)
            preferenceManager.putString(KEY_FIRST_NAME, user.getString(KEY_FIRST_NAME)!!)
            preferenceManager.putString(KEY_LAST_NAME, user.getString(KEY_LAST_NAME)!!)
            preferenceManager.putString(KEY_EMAIL, user.getString(KEY_EMAIL)!!)
            preferenceManager.putString(KEY_PASSWORD, user.getString(KEY_PASSWORD)!!)
        }
    }

    private fun hideSignUpProgress(binding: FragmentSignUpBinding) {
        with(binding) {
            signUpProgress.visibility = View.INVISIBLE
            btnSignUp.visibility = View.VISIBLE
        }
    }

    fun showSignUpProgress(binding: FragmentSignUpBinding) {
        with(binding) {
            btnSignUp.visibility = View.INVISIBLE
            signUpProgress.visibility = View.VISIBLE
        }
    }

    private fun signUpToHomeFragment(binding: FragmentSignUpBinding) {
        findNavController(binding.root)
            .navigate(R.id.action_signUpFragment_to_homeFragment)
    }

    private fun sendFCMTokenToDatabase(token: String) {
        viewModelScope.launch {
            val user = userReference.document(preferenceManager.getString(KEY_USER_ID))
            user.update(KEY_FCM_TOKEN, token).addOnFailureListener { e ->
                Toast.makeText(
                    application,
                    "Unable to send token: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("FCM",e.message.toString())
            }
        }
    }

    fun getNewToken() {
        viewModelScope.launch {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result.toString()
                sendFCMTokenToDatabase(token)
            })
        }
    }

    fun signOut(binding: FragmentHomeBinding) {
        Toast.makeText(application, "Signing out...", Toast.LENGTH_SHORT).show()
        userReference.document(preferenceManager.getString(KEY_USER_ID))
            .update(KEY_FCM_TOKEN, FieldValue.delete())
            .addOnSuccessListener {
                preferenceManager.clearPreferences()
                HomeToSignInFragment(binding)
            }.addOnFailureListener {
                Toast.makeText(application, "Unable to sign out", Toast.LENGTH_LONG).show()
            }
    }

    private fun HomeToSignInFragment(binding: FragmentHomeBinding) {
        findNavController(binding.root)
            .navigate(R.id.action_homeFragment_to_signInFragment)
    }

    fun showSignInProgress(binding: FragmentSignInBinding) {
        with(binding) {
            btnSignIn.visibility = View.INVISIBLE
            signInProgress.visibility = View.VISIBLE
        }
    }

    private fun hideSignInProgress(binding: FragmentSignInBinding) {
        with(binding) {
            signInProgress.visibility = View.INVISIBLE
            btnSignIn.visibility = View.VISIBLE
        }
    }

    fun checkEntryUser(binding: FragmentSignInBinding) {
        if (preferenceManager.getBoolean(KEY_IS_SIGNED_IN)) {
            signInToHomeFragment(binding = binding)
        }
    }

    fun loadUsers(binding: FragmentHomeBinding): LiveData<List<UserWithToken>> {
        viewModelScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            userReference.get().addOnCompleteListener { task ->
                binding.swipeRefreshLayout.isRefreshing = false
                val myUserId = preferenceManager.getString(KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    _listOfUsers.clear()
                    for (documentSnapShot in task.result!!) {
                        if (myUserId != documentSnapShot.id) {
                            addUser(documentSnapShot)
                        }
                    }
                } else {
                    showTextErrorMessage(binding)
                }
            }
        }
        return users
    }

    fun showTextErrorMessage(binding: FragmentHomeBinding) {
        binding.textErrorMessage.apply {
            text = String().format("%s", "No users available")
            visibility = View.VISIBLE
        }
    }

    private fun addUser(documentSnapShot: QueryDocumentSnapshot) {
        val user = UserWithToken(
            documentSnapShot.getString(KEY_FIRST_NAME).toString(),
            documentSnapShot.getString(KEY_LAST_NAME).toString(),
            documentSnapShot.getString(KEY_EMAIL).toString(),
            documentSnapShot.getString(KEY_FCM_TOKEN).toString()
        )
        _listOfUsers.add(user)
        _users.value = _listOfUsers
    }

    fun onMultipleUserAction(isMultipleUsersSelected: Boolean, binding: FragmentHomeBinding){
        viewModelScope.launch {
            if (isMultipleUsersSelected){
                binding.imageConference.visibility = View.VISIBLE
                binding.imageConference.setOnClickListener {
                    val intent =
                        Intent(binding.root.context, OutgoingInvitationActivity::class.java).apply {
                            putExtra(SELECTED_USERS, Gson().toJson(UsersAdapter.selectedUsers))
                            putExtra(TYPE, VIDEO_TYPE)
                            putExtra(IS_MULTIPLE, true)
                        }
                    binding.root.context.startActivity(intent)
                }
            }else{
                binding.imageConference.visibility = View.GONE
            }
        }
    }

//    private fun homeFragmentToOutgoingFragment(binding: FragmentHomeBinding) {
//        val action = HomeFragmentDirections.actionHomeFragmentToOutgoingInvitationFragment(
//            UserWithToken("","","",""),
//            VIDEO_TYPE
//        )
//        findNavController(binding.root)
//            .navigate(action)
//    }

    fun initiateVideoMeeting(user: UserWithToken, binding: FragmentHomeBinding) {
        viewModelScope.launch {
            if (user.token == "null" || user.token.isBlank()) {
                Toast.makeText(
                    application,
                    "${user.firstName + " " + user.lastName} is not available for video meeting",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(binding.root.context, OutgoingInvitationActivity::class.java).apply {
                    putExtra("user", user)
                    putExtra(TYPE, VIDEO_TYPE)
                }
                binding.root.context.startActivity(intent)
//                val action = HomeFragmentDirections.actionHomeFragmentToOutgoingInvitationFragment(
//                    user,
//                    VIDEO_TYPE
//                )
//                findNavController(binding.root)
//                    .navigate(action)
            }
        }
    }

    fun initiateAudioMeeting(user: UserWithToken, binding: FragmentHomeBinding) {
        viewModelScope.launch {
            if (user.token == "null" || user.token.isBlank()) {
                Toast.makeText(
                    application,
                    "${user.firstName + " " + user.lastName} is not available for audio meeting",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(binding.root.context, OutgoingInvitationActivity::class.java).apply {
                    putExtra("user", user)
                    putExtra(TYPE, AUDIO_TYPE)
                }
                binding.root.context.startActivity(intent)
//                val action = HomeFragmentDirections.actionHomeFragmentToOutgoingInvitationFragment(
//                    user,
//                    AUDIO_TYPE
//                )
//                findNavController(binding.root)
//                    .navigate(action)
            }
        }
    }

    fun cancelInvitation(receiverToken: String, activity: Activity) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val data = JSONObject()
            val body = JSONObject()

            data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION_RESPONSE)
            data.put(REMOTE_MSG_INVITATION_RESPONSE, REMOTE_MSG_INVITATION_CANCELLED)

            body.put(REMOTE_MSG_DATA, data)
            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendRemoteMessage(
                body.toString(),
                REMOTE_MSG_INVITATION_RESPONSE,
                activity
            )

        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            activity.onBackPressed()
        }
    }

    private fun sendRemoteMessage(remoteMsgBody: String, type: String, activity: Activity) {
        viewModelScope.launch {
            with(ClientApi) {
                retrofitService.sendRemoteMessage(getRemoteMsgHeaders(), remoteMsgBody)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                if (type == REMOTE_MSG_INVITATION) {
                                    Toast.makeText(
                                        application,
                                        "Invitation send Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else if (type == REMOTE_MSG_INVITATION_RESPONSE) {
                                    Toast.makeText(
                                        application,
                                        "Invitation cancelled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    activity.onBackPressed()
                                }

                            } else {
                                Toast.makeText(application, response.message(), Toast.LENGTH_LONG)
                                    .show()
                                activity.onBackPressed()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(application, t.message, Toast.LENGTH_LONG).show()
                            activity.onBackPressed()
                        }
                    })
            }
        }
    }

    private fun sendInvitationResponseRemoteMessage(
        remoteMsgBody: String,
        type: String,
        activity: Activity
    ) {
        viewModelScope.launch {
            with(ClientApi) {
                retrofitService.sendRemoteMessage(getRemoteMsgHeaders(), remoteMsgBody)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                if (type == REMOTE_MSG_INVITATION_ACCEPTED) {
                                    Toast.makeText(
                                        application,
                                        "Invitation accepted",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else if (type == REMOTE_MSG_INVITATION_REJECTED) {
                                    Toast.makeText(
                                        application,
                                        "Invitation rejected",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    activity.onBackPressed()
                                }
                            } else {
                                Toast.makeText(application, response.message(), Toast.LENGTH_LONG)
                                    .show()
                                activity.onBackPressed()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(application, t.message, Toast.LENGTH_LONG).show()
                            activity.onBackPressed()
                        }
                    })
            }
        }
    }

    fun sendInvitationResponse(type: String, receiverToken: String, activity: Activity) {
        try {
            val tokens = JSONArray()
            tokens.put(receiverToken)

            val data = JSONObject()
            val body = JSONObject()

            data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION_RESPONSE)
            data.put(REMOTE_MSG_INVITATION_RESPONSE, type)

            body.put(REMOTE_MSG_DATA, data)
            body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)

            sendInvitationResponseRemoteMessage(body.toString(), type, activity)

        } catch (e: Exception) {
            Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun initiateMeeting(
        meetingType: String,
        receiverToken: String,
        inviterToken: String,
        receivers: ArrayList<UserWithToken>?,
        binding: ActivityOutgoingInvitationBinding,
        activity: Activity
    ) {
        viewModelScope.launch {
            try {
                val tokens = JSONArray()
                tokens.put(receiverToken)

                if (receivers != null && receivers.size > 0){
                    val userNames = StringBuilder()
                    for (i in receivers){
                        tokens.put(i.token)
                        userNames.append(i.firstName).append(" ").append(i.lastName).append("\n")
                    }
                    binding.apply {
                        textFirstChar.visibility = View.GONE
                        textEmail.visibility = View.GONE
                        textUserName.text = userNames.toString()
                    }
                }
                val body = JSONObject()
                val data = JSONObject()

                data.put(REMOTE_MSG_TYPE, REMOTE_MSG_INVITATION)
                data.put(REMOTE_MSG_MEETING_TYPE, meetingType)
                data.put(KEY_FIRST_NAME, preferenceManager.getString(KEY_FIRST_NAME))
                data.put(KEY_LAST_NAME, preferenceManager.getString(KEY_LAST_NAME))
                data.put(KEY_EMAIL, preferenceManager.getString(KEY_EMAIL))
                data.put(KEY_FCM_TOKEN, inviterToken)
                data.put(REMOTE_MSG_MEETING_ROOM, getMeetingRoom())

                body.put(REMOTE_MSG_DATA, data)
                body.put(REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendRemoteMessage(body.toString(), REMOTE_MSG_INVITATION, activity)
            } catch (e: Exception) {
                Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
                activity.onBackPressed()
            }
        }
    }

    private fun getMeetingRoom(): String {
        _meetingRoom = preferenceManager.getString(KEY_USER_ID) + "_" +
                UUID.randomUUID().toString().substring(0, 5)
        return meetingRoom
    }

    fun onReceiveResponse(activity: Activity): BroadcastReceiver {
        val invitationResponseReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModelScope.launch {
                    val type = intent?.getStringExtra(REMOTE_MSG_INVITATION_RESPONSE)
                    if (type != null) {
                        when (type) {
                            REMOTE_MSG_INVITATION_ACCEPTED -> {
                                Toast.makeText(
                                    context,
                                    "Invitation accepted",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            REMOTE_MSG_INVITATION_REJECTED -> {
                                Toast.makeText(
                                    context,
                                    "Invitation rejected",
                                    Toast.LENGTH_SHORT
                                ).show()
                                activity.onBackPressed()
                            }
                            REMOTE_MSG_INVITATION_CANCELLED -> {
                                Toast.makeText(
                                    activity.application,
                                    "Invitation cancelled",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                Log.d("FCM", "Invitation cancelled")
                                activity.onBackPressed()
                            }
                        }
                    }
                }
            }
        }
        return invitationResponseReceiver
    }

}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class VideoMeetingViewModelFactory(private val application: Application?) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoMeetingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoMeetingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
