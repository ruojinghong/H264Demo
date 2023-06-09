package com.example.demoh264.utils

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.example.demoh264.utils.YUVUtils.nv21ToNv12
import java.nio.ByteBuffer
import kotlin.experimental.and

class H265Encode {

    companion object{
        private const val TAG = "H265Encode"
        private const val NAL_I = 19
        private const val NAL_VPS = 32
        private const val FPS = 15
    }
    private lateinit var mediaCodec:MediaCodec
    //nv21转化为nv12数据
    private var nv12:ByteArray? = null

    //旋转之后的yuv数据
    private var yuv:ByteArray? = null
    private var frameIndex:Long = 0
    private val info = MediaCodec.BufferInfo()
    private var vps_sps_pps_buf:ByteArray? = null
    private var previewWidth  = 0
    private var previewHeight = 0

    fun initEncoder(width:Int,height:Int){
        previewHeight = height
        previewWidth = width
        try {
            //H265编码器 video/hevc
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val mediaFormat  = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC,height,width)
			mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,15)
			mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,800_000)
			mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2)
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE)
            mediaCodec.start()
            val bufferLength = width * height * 3/2
            yuv = ByteArray(bufferLength)

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun encodeFrame(nv21:ByteArray){
        nv12 = YUVUtils.nv21toNv12(nv21)
        //数据旋转90度
        YUVUtils.dataTo90(nv12!!,yuv!!,previewWidth,previewHeight)
        //开始编码
        val inputBufferIndex = mediaCodec.dequeueInputBuffer(10_1000)
        if(inputBufferIndex >= 0){
            val byteBuffer = mediaCodec.getInputBuffer(inputBufferIndex)
            byteBuffer?.clear()
            byteBuffer?.put(yuv)
            //PTS
            // 1. 132的目的是解码器初始化播放器需要时间，防止播放器首帧没有播放的问题，不一定是132us
            // 2.frameIndex 初始值 = 1 不加132也行
            val presentationTimeUs = 132 + frameIndex * 1000_000 /FPS
            mediaCodec.queueInputBuffer(inputBufferIndex,0,yuv!!.size,presentationTimeUs,0)
            frameIndex++
        }

        var outputBufferIndex = mediaCodec.dequeueOutputBuffer(info,10_000)
        while (outputBufferIndex >= 0){
            val byteBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
            dealFrame(byteBuffer!!)
			mediaCodec?.releaseOutputBuffer(outputBufferIndex, false)
			outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(info, 0)
        }
    }

    fun dealFrame(byteBuffer: ByteBuffer){
        //H265的nalu的分割夫的下一个字节类型
        var offset = 4
        if(byteBuffer[2].toInt() == 0x1){
            offset = 3
        }
        //vps sps pps H265的头是2个字节 中间的6位nalu类型
        //0x7E的二进制后八位是 0111 1110
        // int naluType = (byteBuffer.get(offset) & 0x7E) >> 1
        val naluType = byteBuffer[offset].and( 0x7E).toInt().shr(1)
        Log.d(TAG,"naluType = $naluType")
        //保存下载的vps,sps pps
        when(naluType){
            NAL_VPS ->{
                vps_sps_pps_buf = ByteArray(info.size)
                byteBuffer.get(vps_sps_pps_buf!!)
                Log.d(TAG, "vps_sps_pps_buf size =${vps_sps_pps_buf?.size}")
            }
            NAL_I ->{
                // 因为是网络传输，所以在每个i帧之前先发送VPS,SPS,PPS
                val bytes = ByteArray(info.size)
                byteBuffer.get(bytes)
                val newBuf = ByteArray(info.size + vps_sps_pps_buf!!.size)
                System.arraycopy(vps_sps_pps_buf!!, 0, newBuf, 0, vps_sps_pps_buf!!.size)
                System.arraycopy(bytes, 0, newBuf, vps_sps_pps_buf!!.size, bytes.size)

                // 发送
                Log.d(TAG, "send I帧:${newBuf.size}")
                h265DecodeListener?.onDecode(newBuf)
            }
            else ->{
                // 其它bp帧数据
                val bytes = ByteArray(info.size)
                byteBuffer.get(bytes)

                // 发送
                Log.d(TAG, "send P/B帧:${bytes.size}")
                h265DecodeListener?.onDecode(bytes)
            }
        }
    }
    fun releaseEncoder() {
        mediaCodec?.stop()
        mediaCodec?.release()
        Log.d(TAG, "releaseEncoder ok")
    }
    private var h265DecodeListener: IH265DecodeListener? = null
    fun setH265DecodeListener(l: IH265DecodeListener?) {
        this.h265DecodeListener = l
    }
    interface IH265DecodeListener {
        fun onDecode(data: ByteArray?)
    }
}
