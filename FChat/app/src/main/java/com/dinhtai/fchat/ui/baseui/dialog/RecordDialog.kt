package com.dinhtai.fchat.ui.baseui.dialog

import android.content.Context
import android.os.Bundle
import com.dinhtai.fchat.R
import com.dinhtai.fchat.base.BaseBottomSheetDialog
import com.dinhtai.fchat.data.local.media.Recorder
import com.dinhtai.fchat.databinding.DialogRecordBinding
import com.dinhtai.fchat.ui.chat.ChatActivity
import com.dinhtai.fchat.utils.checkAudioPermission
import com.dinhtai.fchat.utils.formatAsTime
import com.dinhtai.fchat.utils.getDrawableCompat
import com.dinhtai.fchat.utils.recordFile
import com.github.squti.androidwaverecorder.WaveConfig
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.sqrt

class RecordDialog(context: Context, chatActivity: ChatActivity, private val onClick: () -> Unit) :
    BaseBottomSheetDialog(context) {
    private lateinit var mContext: Context
    private lateinit var mChatActivity: ChatActivity
    private lateinit var binding: DialogRecordBinding
    private lateinit var recorder: Recorder

    init {
        mContext = context
        mChatActivity = chatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mChatActivity.checkAudioPermission(AUDIO_PERMISSION_REQUEST_CODE)
        initUI()
        setOnDismissListener {
            recorder.release()
        }
    }

    override fun onStart() {
        super.onStart()
        listenOnRecorderStates()
    }

    override fun onStop() {
        recorder.release()
        super.onStop()
    }

    private fun initUI() = with(binding) {
        //ghi am
        recordButton.setOnClickListener { recorder.toggleRecording() }
        visualizer.ampNormalizer = { sqrt(it.toFloat()).toInt() }
    }

    private fun listenOnRecorderStates() = with(binding) {
        recorder = Recorder.getInstance(context).init().apply {
            onStart = { recordButton.icon = mContext.getDrawableCompat(R.drawable.ic_stop_24) }
            onStop = {
                visualizer.clear()
                timelineTextView.text = 0L.formatAsTime()
                recordButton.icon = mContext.getDrawableCompat(R.drawable.ic_record_24)
                onClick()
                WaveRecorder(context.applicationContext.recordFile.toString())
                    .apply { waveConfig = WaveConfig() }
                this@RecordDialog.dismiss()
            }
            onAmpListener = {
                mChatActivity.runOnUiThread {
                    if (recorder.isRecording) {
                        timelineTextView.text = recorder.getCurrentTime().formatAsTime()
//                        ghi
                        visualizer.addAmp(it, tickDuration)
                    }
                }
            }
        }
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
    }
}
