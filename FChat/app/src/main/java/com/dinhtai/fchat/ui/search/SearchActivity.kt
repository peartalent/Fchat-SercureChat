package com.dinhtai.fchat.ui.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doOnTextChanged

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        val adapter = SampleAdapter(supportFragmentManager, viewModel)
        binding.viewPager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        setSupportActionBar(binding.materialToolbar)
        binding.materialToolbar.setNavigationOnClickListener { finish() }
        binding.editSearch.doOnTextChanged { text, start, before, count ->
            viewModel.searchFriends(InfoYourself.token!!, binding.editSearch.text.toString())
            viewModel.searchGroups(binding.editSearch.text.toString())
        }
    }

    inner class SampleAdapter(fm: FragmentManager, private var viewModel: SearchViewModel) :
        FragmentPagerAdapter(fm) {

        override fun getItem(position: Int) = when (position) {
            0 -> SearchFriendFragment(viewModel)
            1 -> SearchGroupFragment(viewModel)
            else -> SearchFriendFragment(viewModel)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> {
                    return "Người dùng"
                }
                1 -> {
                    return "Nhóm"
                }
            }
            return super.getPageTitle(position)
        }

        override fun getCount(): Int = 2
    }
}
