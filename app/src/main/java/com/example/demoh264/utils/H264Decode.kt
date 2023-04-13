package com.example.demoh264.utils

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import kotlin.math.pow

class H264Decode(val mediaBytes: ByteArray, val width: Int, val height: Int, val surface: Surface) :
	Thread() {


	private val TAG = javaClass.name

	var mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

	init {
		//demo测试，一次性读取数据到内存
		val mediaFromat =
			MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
		mediaFromat.setInteger(MediaFormat.KEY_FRAME_RATE, 15)
		mediaCodec.configure(mediaFromat, surface, null, 0)
	}

	override fun run() {
		mediaCodec.start()

		decodeSplitNalu()
		mediaCodec.stop()
		mediaCodec.release()
	}


	private fun decodeSplitNalu() {
		if (mediaBytes == null) return
		//数据开始下标
		var startFrameIndex = 0
		val totalSizeIndex = mediaBytes!!.size.minus(1)
		if (totalSizeIndex != null) {
			Log.i(
				TAG,
				"totalSize is ====>${totalSizeIndex}B ====is ${totalSizeIndex / 2.0.pow(20)}MB"
			)
		}
		val inputBuffers = mediaCodec.inputBuffers
		val info = MediaCodec.BufferInfo()
		while (true) {
			//1ms = 1000us == 1000 000ns
			val inIndex = mediaCodec.dequeueInputBuffer(10_000)
			if (inIndex >= 0) {
				//分割出一帧数据
				if (totalSizeIndex == 0 || startFrameIndex >= totalSizeIndex) {
					Log.e(TAG, "startIndex > = totalSize, break")
					break
				}
				val nextFrameStartIndex = findNextFrame(mediaBytes!!,startFrameIndex+1,totalSizeIndex)
				if(nextFrameStartIndex == -1) break
				//填充数据到buffer
				val byteBuffer = inputBuffers[inIndex]
				byteBuffer.clear()
				byteBuffer.put(mediaBytes!!,startFrameIndex,nextFrameStartIndex-startFrameIndex)
				mediaCodec.queueInputBuffer(inIndex,0,nextFrameStartIndex-startFrameIndex,0,0)
				startFrameIndex = nextFrameStartIndex
			}

			var outIndex = mediaCodec.dequeueOutputBuffer(info,10_000)
			while (outIndex >= 0){
				//这里用简单的时间方式保持视频的fps,否则播放的很快
				try {
				    sleep(33)
				}catch (e:Exception){
					e.printStackTrace()
				}
				//渲染到surface上
				mediaCodec.releaseOutputBuffer(outIndex,true)
				outIndex = mediaCodec.dequeueOutputBuffer(info,0)
			}
		}

	}

	private fun findNextFrame(bytes: ByteArray, startIndex: Int, totalSizeIndex: Int): Int {
		for (i in startIndex..totalSizeIndex) {
			// 00 00 00 01 H264的初始码
			if (bytes[i].toInt() == 0x00 && bytes[i + 1].toInt() == 0x00 && bytes[i + 2].toInt() == 0x00 && bytes[i + 3].toInt() == 0x01) {
					return i
				// 00 00  01 H264的初始码
			}else if (bytes[i].toInt() == 0x00 && bytes[i + 1].toInt() == 0x00 && bytes[i + 2].toInt() == 0x01 	){
				return i
			}
		}

		return -1
	}

	private fun findNextFrameFix(bytes: ByteArray, startIndex: Int, totalSizeIndex: Int):Int{
	// 每次最好数据里大点，不然就像弱网的情况，数据流慢导致视频卡
		val len = startIndex + 40000
		return if(len > totalSizeIndex) totalSizeIndex else len

	}

}
