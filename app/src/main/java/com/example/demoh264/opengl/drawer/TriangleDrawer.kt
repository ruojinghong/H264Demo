package com.example.demoh264.opengl.drawer

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TriangleDrawer():IDrawer {


	//顶点坐标
	private val mVertexCoors = floatArrayOf(-1f,-1f,1f,-1f,0f,1f)
	//纹理坐标
	private val mFragCoors = floatArrayOf(1f,0f,1f,1f,0.5f,0f)

	//纹理ID
	private var mTextureId = -1

	//OpenGl程序ID
	private var mProgramId = -1
	//顶点坐标接受者
	private var mVertexPosHandler = -1
	//纹理坐标接受者
	private var mTexturePosHandler = -1

	private lateinit var mVertexBuffer:FloatBuffer
	private lateinit var mTextureBuffer:FloatBuffer

	init {
	    //初始化顶点坐标
		initPos()
	}

	private fun initPos() {
		val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
		bb.order(ByteOrder.nativeOrder())
		//将坐标数据转化为 FloatBuffer,用以传给OpenGL ES程序
		mVertexBuffer = bb.asFloatBuffer()
		mVertexBuffer.put(mVertexCoors)
		mVertexBuffer.position(0)

		val cc = ByteBuffer.allocateDirect(mFragCoors.size * 4)
		cc.order(ByteOrder.nativeOrder())
		mTextureBuffer = cc.asFloatBuffer()
		mTextureBuffer.put(mFragCoors)
		mTextureBuffer.position(0)
	}

	override fun setVideoSize(width: Int, height: Int) {
		TODO("Not yet implemented")
	}

	override fun setWorldSize(width: Int, height: Int) {
		TODO("Not yet implemented")
	}

	override fun setAlpha(alpha: Float) {
		TODO("Not yet implemented")
	}

	override fun draw() {
		if(mTextureId != -1){
			//【步骤2: 创建、编译并启动OpenGL着色器】
			createGLPrg()
			//【步骤3: 开始渲染绘制】
			doDraw()
		}
	}



	private fun createGLPrg() {
		if(mProgramId == -1){
			val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,getVertexShader())
			val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,getFragmentShader())
			//创建OpenGL ES程序，注意需要再OPenGL渲染线程中创建，否则无法渲染
			mProgramId = GLES20.glCreateProgram()
			//将着色器加载到程序中
			GLES20.glAttachShader(mProgramId,vertexShader)
			GLES20.glAttachShader(mProgramId,fragmentShader)
			//连接着色器
			GLES20.glLinkProgram(mProgramId)
			mVertexPosHandler = GLES20.glGetAttribLocation(mProgramId,"aPosition")
			mTexturePosHandler = GLES20.glGetAttribLocation(mProgramId,"aCoordinate")
		}
		GLES20.glUseProgram(mProgramId)
	}

	private fun doDraw() {
		//启用顶点的句柄
		GLES20.glEnableVertexAttribArray(mVertexPosHandler)
		GLES20.glEnableVertexAttribArray(mTexturePosHandler)
		//设置着色器参数，第二个参数表示一个顶点包含的数据数量这里位xy,
		GLES20.glVertexAttribPointer(mVertexPosHandler,2,GLES20.GL_FLOAT,false,0,mVertexBuffer)
		GLES20.glVertexAttribPointer(mTexturePosHandler, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer)
		//开始绘制
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,3)
	}

	override fun setTextureID(id: Int) {
		mTextureId= id
	}

	override fun release() {
		GLES20.glDisableVertexAttribArray(mVertexPosHandler)
		GLES20.glDisableVertexAttribArray(mTexturePosHandler)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
		GLES20.glDeleteTextures(1, intArrayOf(mTextureId), 0)
		GLES20.glDeleteProgram(mProgramId)
	}

	private fun loadShader(type: Int, shaderCode: String): Int {
		//根据type创建顶点着色器或者片元着色器
		val shader = GLES20.glCreateShader(type)
		//将资源加入到着色器中，并编译
		GLES20.glShaderSource(shader, shaderCode)
		GLES20.glCompileShader(shader)

		return shader
	}

	private fun getVertexShader(): String {
		return "attribute vec4 aPosition;" +
				"void main() {" +
				"  gl_Position = aPosition;" +
				"}"
	}

	private fun getFragmentShader(): String {
		return "precision mediump float;" +
				"void main() {" +
				"  gl_FragColor = vec4(1.0, 1.0, 0.0, 1.0);" +
				"}"
	}
}
