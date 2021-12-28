package com.example.videomeeting.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.videomeeting.databinding.ItemUserBinding
import com.example.videomeeting.listener.UsersListener
import com.example.videomeeting.model.UserWithToken

class UsersAdapter(
    private var usersList: List<UserWithToken>,
    private val usersListener: UsersListener
) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {

    companion object{
        private var _selectedUsers: ArrayList<UserWithToken> = ArrayList()
        val selectedUsers get() = _selectedUsers
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            ItemUserBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = usersList[position]
        holder.bind(user = user, usersListener)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    class UsersViewHolder(
        private var binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            user: UserWithToken,
            usersListener: UsersListener
        ) {
            binding.apply {
                textFirstChar.text = user.firstName.substring(0, 1)
                textEmail.text = user.email
                textUserName.text = String.format("%s %s", user.firstName, user.lastName)
                imageAudioMeeting.setOnClickListener { usersListener.initiateAudioMeeting(user) }
                imageVideoMeeting.setOnClickListener { usersListener.initiateVideoMeeting(user) }
                userContainer.setOnLongClickListener{
                    _selectedUsers.add(user)
                    hideMeetingIcons()
                    usersListener.onMultipleUserAction(true)
                    return@setOnLongClickListener true
                }
                userContainer.setOnClickListener {
                    if (imageSelected.visibility == View.VISIBLE){
                        _selectedUsers.remove(user)
                        showMeetingIcons()
                        if (selectedUsers.size == 0){
                            usersListener.onMultipleUserAction(false)
                        }
                    }else{
                        if (selectedUsers.size > 0){
                            _selectedUsers.add(user)
                            hideMeetingIcons()
                        }
                    }
                }

            }
        }

        private fun hideMeetingIcons() {
            binding.imageSelected.visibility = View.VISIBLE
            binding.textFirstChar.visibility = View.INVISIBLE
            binding.imageVideoMeeting.visibility = View.GONE
            binding.imageAudioMeeting.visibility = View.GONE
        }
        private fun showMeetingIcons() {
            binding.imageSelected.visibility = View.GONE
            binding.textFirstChar.visibility = View.VISIBLE
            binding.imageVideoMeeting.visibility = View.VISIBLE
            binding.imageAudioMeeting.visibility = View.VISIBLE
        }
    }
}