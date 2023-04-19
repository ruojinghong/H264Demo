package com.example.demoh264

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.example.demoh264.databinding.ActivitySocketBinding
import com.example.demoh264.utils.LiveManager

class SocketActivity : AppCompatActivity() {
	lateinit var mViewBinding:ActivitySocketBinding
	lateinit var liveManager: LiveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		mViewBinding = ActivitySocketBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        // localSurfaceView透明
        mViewBinding.localSurfaceView.holder.setFormat(PixelFormat.TRANSPARENT)
        // localSurfaceView放置在顶层，即始终位于最上层
        mViewBinding.localSurfaceView.setZOrderOnTop(true)
        // 简单处理下权限
        requestPermission()

		liveManager = LiveManager(mViewBinding.localSurfaceView.holder,mViewBinding.remoteSurfaceView.holder)

		liveManager.init(540,960)

		initListener()
    }

	private fun initListener() {
		mViewBinding.idStart.setOnClickListener {
			liveManager.start(mViewBinding.checkbox.isChecked)
		}
		mViewBinding.idStop.setOnClickListener {
			liveManager.stop()
		}
	}



	fun requestPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ), 1
            )
        }
        return false
    }
    override fun onDestroy() {
        super.onDestroy()
        liveManager.stop()
    }
}
