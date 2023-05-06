package com.example.demoh264.media.decoder

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.demoh264.extractor.VideoExtractor
import com.example.demoh264.media.BaseDecoder
import com.example.demoh264.media.IExtractor
import java.nio.ByteBuffer

class VideoDecoder(val path:String, val mSurfaceView:SurfaceView?, var mSurface: Surface?):BaseDecoder(path) {

	private val TAG = "VideoDecoder"
	override fun initExtractor(mFilePath: String): IExtractor {
		return VideoExtractor(mFilePath)
	}

	override fun check(): Boolean {
		if(mSurfaceView == null && mSurface == null){
			Log.i(TAG,"sfv && surfaveview不能都为null")
			mStateListener?.decoderError(this, "显示器为空")
			return false
		}
		return true
	}

	override fun initSpecParams(format: MediaFormat) {
	}

	override fun initRender(): Boolean  = true

	override fun configCodec(codec: MediaCodec, format: MediaFormat): Boolean {
		if (mSurface != null) {
			codec.configure(format, mSurface , null, 0)
			notifyDecode()
		} else if (mSurfaceView?.holder?.surface != null) {
			mSurface = mSurfaceView?.holder?.surface
			configCodec(codec, format)
		} else {
			mSurfaceView?.holder?.addCallback(object : SurfaceHolder.Callback2 {
				override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
				}

				override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
				}

				override fun surfaceDestroyed(holder: SurfaceHolder) {
				}

				override fun surfaceCreated(holder: SurfaceHolder) {
					mSurface = holder.surface
					configCodec(codec, format)
				}
			})

			return false
		}
		return true
	}

	override fun render(outputBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
	}

	override fun doneDecode() {
	}
}
