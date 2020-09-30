package com.github.muellerma.coffee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        updateButtonState()
        button.setOnClickListener {
            ForegroundService.startOrStop(this, !ForegroundService.isRunning(this))
            updateButtonState()
        }
    }

    private fun updateButtonState() {
        if (ForegroundService.isRunning(this)) {
            button.setText(R.string.stop_now)
        } else {
            button.setText(R.string.start_now)
        }
    }
}