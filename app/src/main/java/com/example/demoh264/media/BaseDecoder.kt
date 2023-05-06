package com.example.demoh264.media

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.lang.Exception
import java.nio.ByteBuffer

abstract class BaseDecoder(private val mFilePath: String) : IDecoder {

	private val TAG = "BaseDecoder"
	//-------------线程相关--------------------
	/**
	 * 解码器是否正在运行
	 */
	private var mIsRunning = true

	/**
	 * 线程等待🔐
	 *
	 */
	private val mLock = Object()

	/**
	 * 是否可以进入解码
	 */
	private var mReadyForDecode = false
	//--------------解码相关------------------------
	/**
	 * 音视频解码器
	 */
	protected var mCodec: MediaCodec? = null

	/**
	 * 音视频数据读取器
	 */
	protected var mExtractor: IExtractor? = null

	/**
	 * 解码输入缓冲区
	 */
	protected var mInputBuffers: Array<ByteBuffer>? = null

	/**
	 * 解码输出缓冲区
	 */
	protected var mOutputBuffers: Array<ByteBuffer>? = null


	/**
	 * 解码数据信息
	 */
	private var mBufferInfo = MediaCodec.BufferInfo()

	private var mState = DecodeState.STOP

	protected var mStateListener: IDecoderStateListener? = null

	/**
	 * 流程是否结束
	 */
	private var mIsEOS = false
	protected var mVideoHeight = 0
	protected var mVideoWidth = 0

	private var mDuration: Long = 0

	private var mStartPos: Long = 0

	private var mEndPos: Long = 0

	/**
	 * 开始解码时间，用于音视频同步
	 */
	private var mStartTimeForSync = -1L

	// 是否需要音视频渲染同步
	private var mSyncRender = true

	/**
	 * 解码
	 */
	final override fun run() {
		if (mState == DecodeState.STOP) {
			mState = DecodeState.START
		}
		mStateListener?.decoderPrepare(this)

		//【解码步骤：1. 初始化，并启动解码器】
		if (!init()) return

		Log.i(TAG, "开始解码")
		try {
			while (mIsRunning) {
				if (mState != DecodeState.START &&
					mState != DecodeState.DECODING &&
					mState != DecodeState.SEEKING
				) {
					Log.i(TAG, "进入等待：$mState")

					waitDecode()

					// ---------【同步时间矫正】-------------
					//恢复同步的起始时间，即去除等待流失的时间
					mStartTimeForSync = System.currentTimeMillis() - getCurTimeStamp()
				}

				if (!mIsRunning ||
					mState == DecodeState.STOP
				) {
					mIsRunning = false
					break
				}

				if (mStartTimeForSync == -1L) {
					mStartTimeForSync = System.currentTimeMillis()
				}

				//如果数据没有解码完毕，将数据推入解码器解码
				if (!mIsEOS) {
					//【解码步骤：2. 见数据压入解码器输入缓冲】
					mIsEOS = pushBufferToDecoder()
				}

				//【解码步骤：3. 将解码好的数据从缓冲区拉取出来】
				val index = pullBufferFromDecoder()
				if (index >= 0) {
					// ---------【音视频同步】-------------
					if (mSyncRender && mState == DecodeState.DECODING) {
						sleepRender()
					}
					//【解码步骤：4. 渲染】
					if (mSyncRender) {// 如果只是用于编码合成新视频，无需渲染
						render(mOutputBuffers!![index], mBufferInfo)
					}

					//将解码数据传递出去
					val frame = Frame()
					frame.buffer = mOutputBuffers!![index]
					frame.setBufferInfo(mBufferInfo)
					mStateListener?.decodeOneFrame(this, frame)

					//【解码步骤：5. 释放输出缓冲】
					mCodec!!.releaseOutputBuffer(index, true)

					if (mState == DecodeState.START) {
						mState = DecodeState.PAUSE
					}
				}
				//【解码步骤：6. 判断解码是否完成】
				if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
					Log.i(TAG, "解码结束")
					mState = DecodeState.FINISH
					mStateListener?.decoderFinish(this)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			doneDecode()
			release()
		}

	}

	private fun init(): Boolean {
		if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
			Log.w(TAG, "文件路径为空")
			mStateListener?.decoderError(this, "文件路径为空")
			return false
		}

		if (!check()) return false

		//初始化数据提取器
		mExtractor = initExtractor(mFilePath)
		if (mExtractor == null ||
			mExtractor!!.getFormat() == null
		) {
			Log.w(TAG, "无法解析文件")
			return false
		}

		//初始化参数
		if (!initParams()) return false

		//初始化渲染器
		if (!initRender()) return false

		//初始化解码器
		if (!initCodec()) return false
		return true
	}

	abstract fun initExtractor(mFilePath: String): IExtractor


	abstract fun check(): Boolean

	private fun initParams(): Boolean {
		return try {
			val format = mExtractor!!.getFormat()
			mDuration = format!!.getLong(MediaFormat.KEY_DURATION) / 1000
			if (mEndPos == 0L) {
				mEndPos = mDuration
			}
			initSpecParams(format)
			true
		} catch (e: Exception) {
			false
		}
	}

	abstract fun initSpecParams(format: MediaFormat)

	abstract fun initRender(): Boolean

	private fun initCodec(): Boolean {
		try {
			val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
			mCodec = MediaCodec.createDecoderByType(type!!)
			if (!configCodec(mCodec!!, mExtractor!!.getFormat()!!)) {
				waitDecode()
			}
			mCodec!!.start()

			mInputBuffers = mCodec?.inputBuffers
			mOutputBuffers = mCodec?.outputBuffers
		} catch (e: Exception) {
			return false
		}
		return true
	}

	abstract fun configCodec(mediaCodec: MediaCodec, format: MediaFormat): Boolean

	private fun waitDecode() {
		try {
			if (mState == DecodeState.PAUSE) {
				mStateListener!!.decoderPause(this)
			}
			synchronized(mLock) {
				mLock.wait()
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun getCurTimeStamp() = mBufferInfo.presentationTimeUs / 1000

	private fun pushBufferToDecoder(): Boolean {
		val inputBufferIndex = mCodec!!.dequeueInputBuffer(1000L)
		var isEndOfStream = false
		if (inputBufferIndex >= 0) {
			val byteBuffer = mInputBuffers!![inputBufferIndex]
			var sampleSize = mExtractor!!.readBuffer(byteBuffer)
			if (sampleSize < 0) {
				mCodec!!.queueInputBuffer(
					inputBufferIndex, 0, 0,
					0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
				)
				isEndOfStream = true
			} else {
				mCodec!!.queueInputBuffer(
					inputBufferIndex, 0, sampleSize,
					mExtractor!!.getCurrentTimeStamp(), 0
				)
			}
		}

		return isEndOfStream

	}

	private fun pullBufferFromDecoder(): Int {
		// 查询是否有解码完成的数据，index >=0 时，表示数据有效，并且index为缓冲区索引
		var outputBufferIndex = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000L)
		when (outputBufferIndex) {
			MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {}
			MediaCodec.INFO_TRY_AGAIN_LATER -> {}
			MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
				mOutputBuffers = mCodec!!.outputBuffers
			}

			else -> {
				return outputBufferIndex
			}
		}
		return -1;
	}

	private fun sleepRender() {
		val passTime = System.currentTimeMillis() - mStartTimeForSync
		val curTime = getCurTimeStamp()
		if (curTime > passTime) {
			Thread.sleep(curTime - passTime)
		}
	}

	/**
	 * 渲染
	 */
	abstract fun render(
		outputBuffer: ByteBuffer,
		bufferInfo: MediaCodec.BufferInfo
	)

	/**
	 * 结束解码
	 */
	abstract fun doneDecode()

	private fun release() {
		try {
			Log.i(TAG, "解码停止，释放解码器")
			mState = DecodeState.STOP
			mIsEOS = false
			mExtractor?.stop()
			mCodec?.stop()
			mCodec?.release()
			mStateListener?.decoderDestroy(this)
		} catch (e: Exception) {
		}
	}

	/**
	 * 通知解码线程继续运行
	 */
	protected fun notifyDecode() {
		synchronized(mLock) {
			mLock.notifyAll()
		}
		if (mState == DecodeState.DECODING) {
			mStateListener?.decoderRunning(this)
		}
	}


	override fun pause() {
		mState = DecodeState.DECODING
	}

	override fun goOn() {
		mState = DecodeState.DECODING
		notifyDecode()
	}


	override fun seekTo(pos: Long): Long {
		return 0
	}

	override fun seekAndPlay(pos: Long): Long {
		return 0
	}

	override fun stop() {
		mState = DecodeState.STOP
		mIsRunning = false
		notifyDecode()
	}

	override fun isDecoding(): Boolean {
		return mState == DecodeState.DECODING
	}

	override fun isSeeking(): Boolean {
		return mState == DecodeState.SEEKING
	}

	override fun isStop(): Boolean {
		return mState == DecodeState.STOP
	}

	override fun setSizeListener(l: IDecoderProgress) {
	}

	override fun setStateListener(l: IDecoderStateListener?) {
		mStateListener = l
	}

	override fun getWidth(): Int {
		return mVideoWidth
	}

	override fun getHeight(): Int {
		return mVideoHeight
	}

	override fun getDuration(): Long {
		return mDuration
	}


	override fun getRotationAngle(): Int {
		return 0
	}

	override fun getMediaFormat(): MediaFormat? {
		return mExtractor?.getFormat()
	}

	override fun getTrack(): Int {
		return 0
	}

	override fun getFilePath(): String {
		return mFilePath
	}

	override fun withoutSync(): IDecoder {
		mSyncRender = false
		return this
	}
}
