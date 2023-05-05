package com.example.demoh264.socket

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.IOException
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class LiveWebSocketClient : BaseWebSocket() {

	companion object{
		private const val TAG = "LiveWebSocketClient"
		private const val PORT = 30001

		private const val URL = "ws://172.16.100.69:$PORT"
	}

	private var myWebSocketClient:MyWebSocketClient? = null
	override fun sendData(bytes: ByteArray?) {
		if (myWebSocketClient?.isOpen == true) {
			myWebSocketClient?.send(bytes)
		}
	}

	override fun start() {
		try {
			val url = URI(URL)
			myWebSocketClient = MyWebSocketClient(url)
			myWebSocketClient?.connect()
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun release() {
		try {
			myWebSocketClient?.close()
			h265ReceiveListener = null
			Log.d(TAG, "release ok")
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: InterruptedException) {
			e.printStackTrace()
		}
	}

	inner class MyWebSocketClient(serverUri: URI) : WebSocketClient(serverUri){
		override fun onOpen(handshakedata: ServerHandshake?) {
			Log.i(TAG, "onOpen")
		}

		override fun onMessage(message: String?) {
			Log.i(TAG, "onMessage:$message")
		}

		override fun onMessage(bytes: ByteBuffer) {
			if (h265ReceiveListener != null) {
				val buf = ByteArray(bytes.remaining())
				bytes.get(buf)
				Log.i(TAG, "onMessage:" + buf.size)
				h265ReceiveListener?.onReceive(buf)
			}
		}

		override fun onClose(code: Int, reason: String?, remote: Boolean) {
			Log.i(TAG, "onClose: $reason ,code=$code")
		}

		override fun onError(ex: Exception?) {
			Log.i(TAG, "onError: ", ex)
		}

	}
}
