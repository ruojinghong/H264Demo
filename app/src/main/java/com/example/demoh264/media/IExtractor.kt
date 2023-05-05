package com.example.demoh264.media

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * 音视频抽取器
 */
interface IExtractor {
	/**
	 * 获取音视频格式
	 */
	fun getFormat():MediaFormat?

	/**
	 *读取音频数据
	 */
	fun readBuffer(buffer: ByteBuffer):Int

	/**
	 * 获取当前时间戳
	 */
	fun getCurrentTimeStamp():Long

	fun getSampleFlag():Int

	/**
	 * 跳转到指定指定位置，并返回实际时间戳
	 */
	fun seek(pos:Long):Long

	fun setStartPos(pos:Long)

	/**
	 * 停止读取数据
	 */
	fun stop()

}
