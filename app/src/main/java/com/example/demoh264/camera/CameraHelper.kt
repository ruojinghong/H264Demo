package com.example.demoh264.camera

import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import kotlin.math.abs

class CameraHelper(
	private val holder: SurfaceHolder,
	private var width: Int,
	private var height: Int
) : Camera.PreviewCallback {

	lateinit var camera: Camera
	lateinit var buffer: ByteArray
	private var previewListener:IPreviewListener? = null

	companion object {
		const val TAG = "CameraHelper"
	}

	fun startPreview() {
		//临时用后置摄像头，重点是编解码和传输数据
		camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
		val parameters: Camera.Parameters = camera.parameters
		//摄像头默认NV21
		Log.d(TAG, "previewFormat: ${parameters.previewFormat}")
		setpreviewSize(parameters)
		camera.parameters = parameters
		camera.setPreviewDisplay(holder)
		// 由于硬件安装是横着的，如果是后置摄像头&&正常竖屏的情况下需要旋转90度
		// 只是预览旋转了，数据没有旋转
		camera.setDisplayOrientation(90)
		// 让摄像头回调一帧的数据大小
		buffer = ByteArray(width * height * 3 / 2)
		// onPreviewFrame回调的数据大小就是buffer.length
		camera.addCallbackBuffer(buffer)
		camera.setPreviewCallbackWithBuffer(this)
		camera.startPreview()
		previewListener?.onPreviewSize(width, height)
	}

	private fun setpreviewSize(parameters: Camera.Parameters) {
		val supportedPreviewSizes = parameters.supportedPreviewSizes
		var size = supportedPreviewSizes[0]
		Log.d(TAG, "支持 ${size.width}x${size.height}")
		supportedPreviewSizes.removeAt(0)
		var m = abs(size.width * size.height - width * height)
		val iterator = supportedPreviewSizes.iterator()
		while (iterator.hasNext()) {
			val next = iterator.next()
			Log.d(TAG, "支持 ${next.width} x ${next.height}")
			var n = abs(next.height * next.width - width * height)
			if (n < m) {
				m = n
				size = next
			}
		}
		width = size.width
		height = size.height
		parameters.setPreviewSize(width,height)
		//取交集 也就是最小的分辨率
		Log.d(TAG,"最终设置的分辨率为 $width x $height")
	}

	fun stopPreview() {
		if (camera != null) {
			camera.stopPreview()
			camera.release()
			Log.d(TAG, "camera release ok")
		}
	}

	override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
		// 摄像头的原始数据yuv
		previewListener?.onPreviewFrame(data, camera)
		camera?.addCallbackBuffer(data)
	}

	interface IPreviewListener {
		fun onPreviewSize(width: Int, height: Int)
		fun onPreviewFrame(data: ByteArray?, camera: Camera?)
	}

	fun setPreviewListener(previewListener: IPreviewListener?) {
		this.previewListener = previewListener
	}

}
