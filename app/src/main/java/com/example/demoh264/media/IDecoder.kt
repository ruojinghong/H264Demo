package com.example.demoh264.media

import android.media.MediaFormat

/**
 * 解码基类
 */
interface IDecoder : Runnable {
	/**
	 * 暂停解码
	 */
	fun  pause()

	/**
	 * 继续解码
	 */
	fun goOn()

	/**
	 * 停止解码
	 */
	fun stop()

	/**
	 * 是否增在解码
	 */
	fun isDecoding():Boolean

	/**
	 * 是否正在快进
	 */
	fun isSeeking():Boolean

	/**
	 * 是否停止解码
	 */
	fun isStop():Boolean

	/**
	 * 设置监听状态
	 */
	fun  setStateListener(listener:IDecoderStateListener?)

	/**
	 * 获得视频的高
	 */
	fun getHeight():Int
	/**
	 * 获得视频的宽
	 */
	fun getWidth():Int

	/**
	 * 获得视频时长
	 */
	fun  getDuration():Long

	/**
	 * 获取视频旋转角度
	 */
	fun  getRotationAngle():Int

	/**
	 * 获取视频格式参数
	 */
	fun  getMediaFormat():MediaFormat?

	/**
	 * 获取视频音轨
	 */
	fun  getTrack():Int
	/**
	 * 获取解码的文件路径
	 */
	fun  getFilePath():String

	/**
	 * 当前帧时间，单位：ms
	 */
	fun getCurTimeStamp(): Long

	/**
	 * 跳转到指定位置
	 * 并返回实际帧的时间
	 *
	 * @param pos: 毫秒
	 * @return 实际时间戳，单位：毫秒
	 */
	fun seekTo(pos: Long): Long

	/**
	 * 跳转到指定位置,并播放
	 * 并返回实际帧的时间
	 *
	 * @param pos: 毫秒
	 * @return 实际时间戳，单位：毫秒
	 */
	fun seekAndPlay(pos: Long): Long
	/**
	 * 无需音视频同步
	 */
	fun withoutSync(): IDecoder

	/**
	 * 设置尺寸监听器
	 */
	fun setSizeListener(l: IDecoderProgress)
}
