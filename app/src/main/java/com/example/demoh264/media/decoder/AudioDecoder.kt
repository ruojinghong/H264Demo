package com.example.demoh264.media.decoder

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaFormat
import com.example.demoh264.extractor.AudioExtractor
import com.example.demoh264.media.BaseDecoder
import com.example.demoh264.media.IExtractor
import java.nio.ByteBuffer

class AudioDecoder(val path:String) : BaseDecoder(path) {

	/**采样率*/
	private var mSampleRate = -1

	/**声音通道数量*/
	private var mChannels = 1

	/**PCM采样位数*/
	private var mPCMEncodeBit = AudioFormat.ENCODING_PCM_16BIT

	/**音频播放器*/
	private var mAudioTrack: AudioTrack? = null

	/**音频数据缓存*/
	private var mAudioOutTempBuf: ShortArray? = null
	override fun initExtractor(mFilePath: String): IExtractor {
		return AudioExtractor(mFilePath)
	}

	override fun check() = true

	override fun initSpecParams(format: MediaFormat) {
		try {
			mChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
			mSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

			mPCMEncodeBit = if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) {
				format.getInteger(MediaFormat.KEY_PCM_ENCODING)
			} else {
				//如果没有这个参数，默认为16位采样
				AudioFormat.ENCODING_PCM_16BIT
			}
		} catch (e: Exception) {
		}
	}

	override fun initRender(): Boolean {
		val channel = if (mChannels == 1) {
			//单声道
			AudioFormat.CHANNEL_OUT_MONO
		} else {
			//双声道
			AudioFormat.CHANNEL_OUT_STEREO
		}

		//获取最小缓冲区
		val minBufferSize = AudioTrack.getMinBufferSize(mSampleRate, channel, mPCMEncodeBit)

		mAudioOutTempBuf = ShortArray(minBufferSize/2)

		mAudioTrack = AudioTrack(
			AudioManager.STREAM_MUSIC,//播放类型：音乐
			mSampleRate, //采样率
			channel, //通道
			mPCMEncodeBit, //采样位数
			minBufferSize, //缓冲区大小
			AudioTrack.MODE_STREAM) //播放模式：数据流动态写入，另一种是一次性写入

		mAudioTrack!!.play()
		return true
	}

	override fun configCodec(mediaCodec: MediaCodec, format: MediaFormat): Boolean {
		mediaCodec.configure(format, null , null, 0)
		return true
	}

	override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
		if (mAudioOutTempBuf!!.size < bufferInfo.size / 2) {
			mAudioOutTempBuf = ShortArray(bufferInfo.size / 2)
		}
		outputBuffer.position(0)
		outputBuffer.asShortBuffer().get(mAudioOutTempBuf, 0, bufferInfo.size/2)
		mAudioTrack!!.write(mAudioOutTempBuf!!, 0, bufferInfo.size / 2)
	}

	override fun doneDecode() {
		mAudioTrack?.stop()
		mAudioTrack?.release()
	}
}
