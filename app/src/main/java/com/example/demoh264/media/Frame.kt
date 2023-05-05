package com.example.demoh264.media

import android.media.MediaCodec
import java.nio.ByteBuffer

class Frame {
		var buffer: ByteBuffer? = null
		val bufferInfo = MediaCodec.BufferInfo()

	fun setBufferInfo(info:MediaCodec.BufferInfo){
		bufferInfo.set(info.offset,info.size,info.presentationTimeUs,info.flags)
	}
}
