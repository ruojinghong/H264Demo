package com.example.demoh264.opengl

import android.opengl.GLES20

/**
 * openGL常用方法工具类
 *
 */
object OpenGLTools {

	fun createTextureIds(count:Int):IntArray{
		val intArray = IntArray(count)
		GLES20.glGenTextures(count,intArray,0)//生成纹理
		return intArray
	}
}
