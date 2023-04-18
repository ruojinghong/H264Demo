package com.example.demoh264.utils

import android.view.SurfaceHolder
import com.example.demoh264.camera.CameraHelper
import com.example.demoh264.socket.BaseWebSocket

class LiveManager(private val localHolder:SurfaceHolder,private val remoteHolder:SurfaceHolder) {

	private lateinit var cameraHelper: CameraHelper
	private var webSocket:BaseWebSocket

}
