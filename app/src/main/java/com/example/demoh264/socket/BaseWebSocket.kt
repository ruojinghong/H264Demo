package com.example.demoh264.socket

abstract class BaseWebSocket {

	abstract fun sendData(bytes:ByteArray?)

	abstract fun start()

	abstract fun release()
	var h265ReceiveListener: IH265ReceiveListener? = null

	@JvmName("setH265ReceiveListener1")
	fun setH265ReceiveListener(h265ReceiveListener: IH265ReceiveListener?) {
		this.h265ReceiveListener = h265ReceiveListener
	}

	interface IH265ReceiveListener {
		fun onReceive(data: ByteArray?)
	}
}
