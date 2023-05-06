package com.example.demoh264

import android.media.MediaRecorder.VideoEncoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.example.demoh264.databinding.ActivityDecoderBinding
import com.example.demoh264.media.decoder.AudioDecoder
import com.example.demoh264.media.decoder.VideoDecoder
import java.util.concurrent.Executors

class DecoderActivity : AppCompatActivity() {

	private lateinit var mBinding:ActivityDecoderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		mBinding = ActivityDecoderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

		initPlayer()

    }

	private fun initPlayer() {
		val path = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Camera/20230319_124624.mp4"

		val threadPool = Executors.newFixedThreadPool(2)

		val videoDecoder = VideoDecoder(path, mBinding.surfaceView, null)
		val audioDecoder = AudioDecoder(path)

		threadPool.execute(videoDecoder)
		threadPool.execute(audioDecoder)

		videoDecoder.goOn()
		audioDecoder.goOn()


	}
}
