package com.dinhtai.fchat.ui.search

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BindingFragment
import com.dinhtai.fchat.data.local.Group
import com.dinhtai.fchat.databinding.FragmentSearchGroupBinding
import com.dinhtai.fchat.ui.baseui.adapter.GroupAdapter
import com.dinhtai.fchat.ui.chat.ChatActivity

class SearchGroupFragment(private var v: SearchViewModel) : BindingFragment<FragmentSearchGroupBinding>() {
    override fun getLayoutResId() = R.layout.fragment_search_group

    override val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    override fun setupView() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.vm = v
        binding.recyclerGroup.adapter = GroupAdapter(::onItemClick)
    }
    private fun onItemClick(group: Group) {
        group?.let {
            var intent = Intent(requireContext(),ChatActivity::class.java)
            intent.putExtra("groupId",group.id)
            startActivity(intent)
        }
    }
}
