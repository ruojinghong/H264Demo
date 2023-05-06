package com.example.demoh264.extractor

import android.media.MediaFormat
import com.example.demoh264.media.IExtractor
import java.nio.ByteBuffer

class VideoExtractor(val path:String):IExtractor {

	private val mExtractor = MMExtractor(path)
	override fun getFormat(): MediaFormat? {
		return mExtractor.getVideoFromat()
	}

	override fun readBuffer(buffer: ByteBuffer): Int {
		return mExtractor.readBuffer(buffer)
	}

	override fun getCurrentTimeStamp(): Long {
		return mExtractor.getCurrentTimestamp()
	}

	override fun getSampleFlag(): Int {
		return  mExtractor.getSampleFlag()
	}

	override fun seek(pos: Long): Long {
		return mExtractor.seek(pos)
	}

	override fun setStartPos(pos: Long) {
		return mExtractor.setStartPos(pos)
	}

	override fun stop() {
		mExtractor.stop()
	}
}
