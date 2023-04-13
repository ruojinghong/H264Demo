package com.example.demoh264

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.FileUtils
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceHolder.Callback
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import com.example.demoh264.databinding.ActivityMainBinding
import com.example.demoh264.utils.CodecUtils
import com.example.demoh264.utils.H264Decode
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.Permission

class MainActivity : AppCompatActivity() {


	// 抽取aac音频文件
	// ffmpeg -i douyin.MP4 -acodec copy -vn  douyin.aac
	// 抽取H264视频文件
	// ffmpeg -i douyin.MP4  -c:v copy -bsf:v h264_mp4toannexb -an  douyin.h264
	var byteeArray: ByteArray? = null
//	var path = "/storage/emulated/0/DCIM/Video/RadagonOfTheGoldenOrder.mp4"
//	var path = "/storage/emulated/0/DCIM/Video/douyin.mp4"
	var path = "douyin.h264"
	private lateinit var surfaceHolder: SurfaceHolder
	private lateinit var mediaProjectionManager:MediaProjectionManager

	init {
		val TAG = "H264"


	}

	private lateinit var binding: ActivityMainBinding
	private lateinit var registerForActivityResult :ActivityResultLauncher<Intent>


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		byteeArray = assets.open(path).readBytes()
		Log.i("vedio size", "${byteeArray?.size}")
		// Example of a call to a native method
		binding.sampleText.text = stringFromJNI()
		CodecUtils.getSupportCodeC()
		checkPermission()
		initSurface(720,1080)
		mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
		initLisener()
	}

	private fun initLisener() {
		binding.startRecord.setOnClickListener{
			startRecord()
		}
		binding.stopRecord.setOnClickListener{
			stopRecord()
		}
		 registerForActivityResult =
			registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
				if(it.resultCode == Activity.RESULT_OK){

				}
			}

	}

	fun startRecord(){


	}

	fun stopRecord(){

	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

		}
	}

	/**
	 * A native method that is implemented by the 'demoh264' native library,
	 * which is packaged with this application.
	 */
	external fun stringFromJNI(): String

	companion object {
		// Used to load the 'demoh264' library on application startup.
		init {
			System.loadLibrary("demoh264")
		}
	}


	private fun decodeSplitNalu(){


	}

	fun play(view: View){
		H264Decode( assets.open(path).readBytes(),720,1080,surfaceHolder.surface).start()
	}

	private fun checkPermission() {
		// 简单处理下权限
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			) != PackageManager.PERMISSION_GRANTED
		) {
			val permissions = arrayOf(
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
			)
			requestPermissions(permissions, 1)
		}
	}

	private fun initSurface(width:Int,h:Int){
		binding.surface.holder.addCallback(object :Callback{
			override fun surfaceCreated(holder: SurfaceHolder) {
				surfaceHolder = holder

			}

			override fun surfaceChanged(
				holder: SurfaceHolder,
				format: Int,
				width: Int,
				height: Int
			) {

			}

			override fun surfaceDestroyed(holder: SurfaceHolder) {

			}
		})
	}



}