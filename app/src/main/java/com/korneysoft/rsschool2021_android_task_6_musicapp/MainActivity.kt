package com.korneysoft.rsschool2021_android_task_6_musicapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.korneysoft.rsschool2021_android_task_6_musicapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding :ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding= ActivityMainBinding.inflate(layoutInflater)
    }
}
