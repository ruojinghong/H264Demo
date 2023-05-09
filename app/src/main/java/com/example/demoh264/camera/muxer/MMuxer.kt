package com.example.demoh264.camera.muxer

import android.media.MediaCodec.BufferInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Android原生提供了一个封装器MediaMuxer，用于将已经编码好的音视频流数据封装到指定格式的文件中，MediaMuxer支持MP4、Webm、3GP三种封装格式。一般使用MP4格式。
 */
class MMuxer {

	private val TAG = "mmuxer"

	private var mPath: String

	private var mMediaMuxer: MediaMuxer? = null

	private var mVideoTrackIndex = -1
	private var mAudioTrackIndex = -1

	private var mIsAudioTrackAdd = false
	private var mIsVideoTrackAdd = false

	private var mIsAudioEnd = false
	private var mIsVideoEnd = false

	private var mIsStart = false


	private var mStateListener: IMuxerStateListener? = null

	init {
		val fileName = "LVideo_Test" + SimpleDateFormat("yyyyMM_dd-HHmmss").format(Date()) + ".mp4"
		val filePath = Environment.getExternalStorageDirectory().absolutePath.toString() + "/DCIM/Camera/"
		mPath = filePath + fileName
		mMediaMuxer = MediaMuxer(mPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
	}

	fun addVideoTrack(mediaFormat: MediaFormat) {
		if (mIsVideoTrackAdd) return
		if (mMediaMuxer != null) {
			mVideoTrackIndex = mMediaMuxer!!.addTrack(mediaFormat)
		}
		mIsVideoTrackAdd = true
		startMuxer()
	}

	fun setListener(listener: IMuxerStateListener) {
		mStateListener = listener
	}


	fun addAudioTrack(mediaFormat: MediaFormat) {
		if (mIsAudioTrackAdd) return
		if (mMediaMuxer != null) {
			mAudioTrackIndex = mMediaMuxer!!.addTrack(mediaFormat)
		}
		mIsAudioTrackAdd = true
		startMuxer()
	}


	fun setNoAudio() {
		if (mIsAudioTrackAdd) return
		mIsAudioTrackAdd = true
		mIsAudioEnd = true
		startMuxer()
	}

	fun setNoVideo() {
		if (mIsVideoTrackAdd) return
		mIsVideoTrackAdd = true
		mIsVideoEnd = true
		startMuxer()
	}

	private fun startMuxer() {
		if (mIsAudioTrackAdd && mIsVideoTrackAdd) {
			mMediaMuxer?.start()
			mIsStart = true
			mStateListener?.onMuxerStart()
			Log.i(TAG, "启动封装器")
		}
	}

	fun releaseVideoTrack() {
		mIsVideoEnd = true
		release()
	}

	fun releaseAudioTrack() {
		mIsAudioEnd = true
		release()
	}

	private fun release() {
		if (mIsVideoEnd && mIsAudioEnd) {
			mIsAudioTrackAdd = false
			mIsAudioTrackAdd = false
			mMediaMuxer?.stop()
			mMediaMuxer?.release()
			mMediaMuxer = null
			Log.i(TAG, "退出封装器")
			mStateListener?.onMuxerFinish()
		}
	}

	interface IMuxerStateListener {
		fun onMuxerStart() {}
		fun onMuxerFinish() {}
	}

	fun writeVideoData(byteBuffer: ByteBuffer,bufferInfo: BufferInfo){
		if(mIsStart && mVideoTrackIndex != -1 && mAudioTrackIndex != -1){
			mMediaMuxer?.writeSampleData(mVideoTrackIndex,byteBuffer,bufferInfo)
		}
	}

	fun writeAudioData(byteBuffer: ByteBuffer,bufferInfo: BufferInfo){
		if(mIsStart && mVideoTrackIndex != -1 && mAudioTrackIndex != -1){
			mMediaMuxer?.writeSampleData(mAudioTrackIndex,byteBuffer,bufferInfo)
		}
	}

}
