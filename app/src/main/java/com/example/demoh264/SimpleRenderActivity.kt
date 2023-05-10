package com.example.demoh264

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.demoh264.databinding.ActivitySimpleRenderBinding
import com.example.demoh264.opengl.drawer.IDrawer
import com.example.demoh264.opengl.drawer.SimpleRender
import com.example.demoh264.opengl.drawer.TriangleDrawer

/**
 * OpenGL simple  demo
 */
class SimpleRenderActivity : AppCompatActivity() {
	companion object{
		const val GL_TYPE = "gl_Type"
	}

	private lateinit var mBinding:ActivitySimpleRenderBinding
	lateinit var  drawer:IDrawer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		mBinding = ActivitySimpleRenderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
		var  intExtra = intent.getIntExtra(GL_TYPE, 0)
		when(intExtra){
			0 ->{drawer = TriangleDrawer()}
			else ->{}

		}
		initRender(drawer)
    }
	private fun initRender(drawer:IDrawer){
		mBinding.glSurface.setEGLContextClientVersion(2)
		mBinding.glSurface.setRenderer(SimpleRender(drawer))
	}

	override fun onDestroy() {
		drawer.release()
		super.onDestroy()
	}
}
