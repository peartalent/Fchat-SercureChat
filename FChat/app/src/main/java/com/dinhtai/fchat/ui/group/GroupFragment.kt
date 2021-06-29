package com.dinhtai.fchat.ui.group

import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.Group
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.FragmentGroupBinding
import com.dinhtai.fchat.network.NetworkConnection
import com.dinhtai.fchat.network.socket.WebSocketClient
import com.dinhtai.fchat.ui.baseui.adapter.GroupAdapter
import com.dinhtai.fchat.ui.group.create.CreateGroupActivity
import com.dinhtai.fchat.ui.search.SearchActivity
import com.dinhtai.fchat.utils.config.ConfigAPI
import com.dinhtai.fchat.utils.config.ConfigMessage
import com.google.gson.JsonObject
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class GroupFragment : BindingFragment<FragmentGroupBinding>(), SwipeRefreshLayout.OnRefreshListener {
    override fun getLayoutResId(): Int = R.layout.fragment_group

    override val viewModel: GroupViewModel by lazy {
        ViewModelProvider(this)[GroupViewModel::class.java]
    }
    private lateinit var networkConnection: NetworkConnection
    private lateinit var adapter: GroupAdapter
    override fun onResume() {
        super.onResume()
        InfoYourself.token?.let {
            viewModel.getAllGroup(it)
        }
    }

    override fun onStart() {
        super.onStart()
        InfoYourself.token?.let {
            viewModel.getAllGroup(it)
        }
    }
    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        networkConnection = NetworkConnection(requireContext())
        networkConnection.observe(this,{isConnect->
            if (isConnect){
                WebSocketClient().connectSocket(ConfigAPI.WS_URL_CHAT, SocketListener())
            } else{
                showToast("Đã có lỗi, k kết nối được internet")
            }
        })
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        InfoYourself.token?.let {
            viewModel.getAllGroup(it)
        }
        binding.groupVM = viewModel
        binding.buttonCreateGroup.setOnClickListener {
            startActivity(Intent(activity, CreateGroupActivity::class.java))
        }
        adapter =GroupAdapter(::onItemClick)
        binding.recyclerGroup.adapter =  adapter
        binding.buttonSearch.setOnClickListener { startActivity(Intent(requireContext(),SearchActivity::class.java)) }
        viewModel.groups.observe(this,{
            adapter.notifyDataSetChanged()
        })
    }

    private fun onItemClick(group: Group) {
        group?.let {
            findNavController().navigate(
                GroupFragmentDirections.actionGroupFragmentToChatActivity(groupId = group.id)
            )
        }
    }

    inner class SocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            requireActivity().runOnUiThread(Runnable {
                var json = JsonObject()
                InfoYourself.token?.let { token ->
                    json.addProperty("token", token)
                    json.addProperty("type_data", ConfigMessage.USER_LOGIN)
                    webSocket.send(json.toString())
                }
                InfoYourself.token?.let {
                    viewModel.getAllGroup(it)
                }
            })
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            InfoYourself.token?.let {
                viewModel.getAllGroup(it)
            }
        }
    }
    fun refesh(){
        InfoYourself.token?.let {
            viewModel.getAllGroup(it)
        }
    }
    override fun onRefresh() {
        refesh()
        Handler().postDelayed(object : Runnable {
            override fun run() {
                binding.swipeRefreshLayout.isRefreshing = (false)
            }

        }, 1500)
    }
}
