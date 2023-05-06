package com.example.demoh264.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

class MMExtractor(var path: String) {
	/**音视频分离器*/
	private var mmExtractor: MediaExtractor? = null

	/**音频通道索引*/
	private var mAudioTrack = -1

	/**视频通道索引*/
	private var mVideoTrack = -1

	/**当前帧时间戳*/
	private var mCurSampleTime = 0L

	/**当前帧标记*/
	private var mCurFrameFLag = 0

	/**开始解码的时间*/
	private var mStartPos = 0L

	init {
		mmExtractor = MediaExtractor();
		mmExtractor?.setDataSource(path)
	}

	/**
	 * 获取视频格式参数
	 */

	fun getVideoFromat(): MediaFormat? {
		for (i in 0 until mmExtractor!!.trackCount) {
			var trackFormat = mmExtractor!!.getTrackFormat(i)
			var string = trackFormat.getString(MediaFormat.KEY_MIME)
			if (string != null) {
				if (string.startsWith("video/")) {
					mVideoTrack = i
					break
				}
			}
		}
		return if (mVideoTrack >= 0) {
			mmExtractor!!.getTrackFormat(mVideoTrack)
		} else {
			null
		}

	}

	fun getAudioFormat(): MediaFormat? {
		for (i in 0 until mmExtractor!!.trackCount) {
			var trackFormat = mmExtractor!!.getTrackFormat(i)
			var string = trackFormat.getString(MediaFormat.KEY_MIME)
			if (string != null) {
				if (string.startsWith("audio/")) {
					mAudioTrack = i
					break
				}
			}
		}
		return if (mAudioTrack >= 0) mmExtractor!!.getTrackFormat(mAudioTrack)
		else null
	}

	fun  readBuffer(byteBuffer: ByteBuffer):Int{
		byteBuffer.clear()
		selectSourceTrack()
		var readSampleCount = mmExtractor!!.readSampleData(byteBuffer, 0)
		if (readSampleCount < 0) {
			return -1
		}
		//记录当前帧的时间戳
		mCurSampleTime = mmExtractor!!.sampleTime
		mCurFrameFLag = mmExtractor!!.sampleFlags
		//进入下一帧
		mmExtractor!!.advance()
		return readSampleCount
	}

	/**
	 * 选择通道
	 */
	private fun selectSourceTrack() {
		if (mVideoTrack >= 0) {
			mmExtractor!!.selectTrack(mVideoTrack)
		} else if (mAudioTrack >= 0) {
			mmExtractor!!.selectTrack(mAudioTrack)
		}
	}


	/**
	 * Seek到指定位置，并返回实际帧的时间戳
	 */
	fun seek(pos: Long): Long {
		mmExtractor!!.seekTo(pos, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
		return mmExtractor!!.sampleTime
	}

	/**
	 * 停止读取数据
	 */
	fun stop() {
		mmExtractor?.release()
		mmExtractor = null
	}

	fun getVideoTrack(): Int {
		return mVideoTrack
	}

	fun getAudioTrack(): Int {
		return mAudioTrack
	}

	fun setStartPos(pos: Long) {
		mStartPos = pos
	}

	/**
	 * 获取当前帧时间
	 */
	fun getCurrentTimestamp(): Long {
		return mCurSampleTime
	}

	fun getSampleFlag(): Int {
		return mCurFrameFLag
	}

}
