package com.dinhtai.fchat.ui.image

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.dinhtai.fchat.data.local.InfoYourself
import com.dinhtai.fchat.data.local.Message
import com.dinhtai.fchat.databinding.ActivityImageBinding
import com.dinhtai.fchat.ui.baseui.adapter.ImageAdapter
import com.dinhtai.fchat.ui.chat.ChatViewModel
import com.dinhtai.fchat.utils.file.FileExternal


class ImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageBinding
    val viewModel: ChatViewModel by lazy {
        ViewModelProvider(this)[ChatViewModel::class.java]
    }
    private var id =-1
    private val fileExternal: FileExternal by lazy { FileExternal(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        var intent = intent
        binding.vm = viewModel
        id =  intent.getIntExtra("id",-1)
        if (intent.getStringExtra("type").equals("group")){
            InfoYourself.token?.let { token ->
                viewModel.getMessageGroupById(token,intent.getIntExtra("id",-1))
            }
            var key = fileExternal.readFileAES("group" + id)
            binding.recyclerImages.adapter = key?.let { ImageAdapter(::onClickItem, it) }
        }  else{
            InfoYourself.token?.let { token ->
                intent.getStringExtra("idUser")?.let { viewModel.getMessagesById(token, it) }
            }
            var key = fileExternal.readFileAES(intent.getStringExtra("idUser")!!)
            binding.recyclerImages.adapter = key?.let { ImageAdapter(::onClickItem, it) }
        }
        var index = intent.getIntExtra("index",0)
        viewModel.messageImages.observe(this, Observer {
            binding.recyclerImages.scrollToPosition(index)
        })
//        binding.recyclerImages.affectOnItemClicks { position, view -> }

//      recycle horizontal giống view page
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        binding.recyclerImages.setLayoutManager(layoutManager)
        snapHelper.attachToRecyclerView(binding.recyclerImages)
        binding.materialToolbar.setNavigationOnClickListener {
            finish()
        }

    }

    private fun onClickItem(message: Message) {
    }
}
