package com.example.demoh264.utils

import android.hardware.display.DisplayManager
import android.icu.text.ListFormatter.Width
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.os.Environment
import android.util.Log
import java.io.FileOutputStream
import kotlin.experimental.and


class H264Encode(private val mediaProjection: MediaProjection, val path: String) :
	Thread("encode_h264") {


	var mediaCodec: MediaCodec

	val mediaInfo = MediaCodec.BufferInfo()

	private val fos = FileOutputStream(path)

	@Volatile
	var isStop = false

	companion object {
		const val TAG = "H264Encode"
		const val WIDTH = 1440
		const val HEIGHT = 3040
	}


	init {
		//init H264/avc  encoder
		mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
		val mediaFormat =
			MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, WIDTH, HEIGHT)
		mediaFormat.setInteger(
			MediaFormat.KEY_COLOR_FORMAT,
			MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
		)
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, WIDTH * HEIGHT * 30)
		mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

		var surface = mediaCodec.createInputSurface()
		mediaProjection.createVirtualDisplay(
			"screen-h264",
			WIDTH,
			HEIGHT,
			2,
			DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
			surface,
			null,
			null
		)
	}


	override fun run() {
		super.run()
		try {
			while (!isStop) {
//				mediaCodec.queueInputBuffer(0, 0, 0, 0, 0)
				val outIndex = mediaCodec.dequeueOutputBuffer(mediaInfo, 10_000)
				if (outIndex >= 0) {
					//取出编码后的h264数据
					val outBuffer = mediaCodec.getOutputBuffer(outIndex)
					val data = ByteArray(mediaInfo.size)
					outBuffer?.get(data)
					Log.d(TAG,"outIndex value =====> $outIndex")
					check(data)
					fos.write(data)

					mediaCodec.releaseOutputBuffer(outIndex, false)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			fos.flush()
			fos.close()
			mediaProjection.stop()
			mediaCodec.stop()
			mediaCodec.release()
		}
	}

	private fun check(bytes: ByteArray) {
		Log.d(TAG, "bytes ====> ${bytes.size}")
		var index = 4 // 00 00 00 01
		if (bytes[2].toInt() == 0x1) {
			index = 3
		}
		// NALU的数据类型,header 1个字节的后五位
		val naluType = (bytes[index].and(0x1f)).toInt()
		when (naluType) {
			7 -> {
				Log.d(TAG, "SPS")
			}

			8 -> {
				Log.d(TAG, "PPS")
			}

			5 -> {
				Log.d(TAG, "IDR")
			}

			else -> {
				Log.d(TAG, "!IDR =====> $naluType")
			}
		}

	}

	fun startEncode() {
		isStop = false
		mediaCodec.start()
		start()
	}

	fun stopEncode() {
		isStop = true
	}

}
