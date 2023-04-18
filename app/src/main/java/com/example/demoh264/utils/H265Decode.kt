package com.example.demoh264.utils

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

class H265Decode {

    private lateinit var mediaCodec: MediaCodec
    private val info = MediaCodec.BufferInfo()
    private val FRAME_RATE = 30

    companion object {
        private const val TAG = "H265Decode"
    }

    fun initDecode(surface: Surface?, width: Int, height: Int) {
        mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * FRAME_RATE)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)

        //渲染到surface上
        mediaCodec.configure(mediaFormat, surface, null, 0)
        mediaCodec.start()

    }

    fun decode(data: ByteArray) {
        val index = mediaCodec.dequeueInputBuffer(10_000)
        if (index >= 0) {
            //送入数据
            val byteBuffer = mediaCodec.getInputBuffer(index)
            byteBuffer?.clear()
            byteBuffer?.put(data)
            mediaCodec.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
        }
        var outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 10_000)
        while (outputBufferIndex >= 0) {
            //ture 渲染到surface上
            mediaCodec.releaseOutputBuffer(outputBufferIndex, true)
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 0)
        }
    }

    fun releaseDecoder() {
        try {
            if (mediaCodec != null) {
                mediaCodec.stop()
                mediaCodec.release()
                Log.d(TAG, "releaseDecoder ok")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}