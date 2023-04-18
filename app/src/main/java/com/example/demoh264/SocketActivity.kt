package com.example.demoh264

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.example.demoh264.databinding.ActivitySocketBinding
import com.example.demoh264.utils.LiveManager

class SocketActivity : AppCompatActivity() {
	lateinit var mViewBinding:ViewBinding
	lateinit var liveManager: LiveManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		mViewBinding = ActivitySocketBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
    }
}
