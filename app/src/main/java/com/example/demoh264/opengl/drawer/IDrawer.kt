package com.example.demoh264.opengl.drawer

import android.graphics.SurfaceTexture

/**
 * 渲染器
 */
interface IDrawer {

	fun  setVideoSize(width:Int,height:Int)
	fun  setWorldSize(width:Int,height:Int)

	fun setAlpha(alpha:Float)

	fun draw()
	fun setTextureID(id:Int)

	fun getSurfaceTexture(cb: (st:SurfaceTexture) -> Unit){}

	fun  release()

}
