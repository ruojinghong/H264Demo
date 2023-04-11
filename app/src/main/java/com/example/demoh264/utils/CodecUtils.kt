package com.example.demoh264.utils

import android.media.MediaCodecList
import android.util.Log

object CodecUtils {

	fun getSupportCodeC() {
		var allCodecs = MediaCodecList(MediaCodecList.REGULAR_CODECS)
		var codecInfos = allCodecs.codecInfos
		for (code in codecInfos) {
			if (code.isEncoder) {
				Log.i("Encode ===== >", code.name)
			}
		}
		for (code in codecInfos) {
			if (!code.isEncoder) {
				Log.i("Decode ===== >", code.name)
			}
		}
	}



}
