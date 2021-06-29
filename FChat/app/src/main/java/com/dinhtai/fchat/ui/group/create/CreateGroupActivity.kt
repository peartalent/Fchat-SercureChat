package com.dinhtai.fchat.ui.group.create

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.dinhtai.fchat.R
import com.dinhtai.fchat.data.local.User
import com.dinhtai.fchat.databinding.ActivityCreateGroupBinding
import com.dinhtai.fchat.ui.baseui.adapter.AddFriendToNewGroupAdapter
import com.dinhtai.fchat.ui.baseui.adapter.MemberGroupAdapter
import com.dinhtai.fchat.ui.friend.FriendViewModel
import com.dinhtai.fchat.utils.toast
import java.io.Serializable

class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupBinding
    private val viewModelFriend: FriendViewModel by lazy {
        ViewModelProvider(this)[FriendViewModel::class.java]
    }
    private val viewModel: CreateGroupViewModel by lazy {
        ViewModelProvider(this)[CreateGroupViewModel::class.java]
    }
    var adapter = AddFriendToNewGroupAdapter(::onRemoteItemClick)
    private var users: MutableList<User> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.friendVM = viewModelFriend
        binding.createGroupVM = viewModel

        binding.recyclerFriend.adapter = MemberGroupAdapter(::onAddItemClick)
        adapter.setData(users)
        binding.recyclerMember.adapter = adapter
        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.buttonContinue.setOnClickListener {
            if (users.size>0){
                var intent = Intent(this,CompleteCreateGroupActivity::class.java)
                intent.putExtra("member",users as Serializable)
                startActivity(intent)
            }else{
                toast("Chưa có thành viên nào")
            }
        }
    }

    private fun onRemoteItemClick(user: User) {
        users.remove(user)
        adapter.notifyDataSetChanged()
        Log.d("remove", users.toString())
        binding.textMember.text = resources.getString(R.string.lable_member) +" "+users.size
    }

    private fun onAddItemClick(user: User) {
        if (!users.contains(user)) {
            users.add(user)
            adapter.notifyDataSetChanged()
            Log.d("add", users.toString())
        } else {
            users.remove(user)
            adapter.notifyDataSetChanged()
            Log.d("remove", users.toString())
        }
        binding.textMember.text = resources.getString(R.string.lable_member) +" "+users.size
    }
}
