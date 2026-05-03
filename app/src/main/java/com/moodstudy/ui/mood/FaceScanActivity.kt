package com.moodstudy.ui.mood

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.moodstudy.databinding.ActivityFaceScanBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceScanBinding
    private lateinit var cameraExecutor: ExecutorService

    private var isMoodDetected = false
    private var detectionCount = 0
    private val moodVotes = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCancel.setOnClickListener { finish() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }
    }

    private fun startCamera() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val cameraProvider: ProcessCameraProvider = withContext(Dispatchers.IO) {
                    ProcessCameraProvider.getInstance(this@FaceScanActivity).get()
                }

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor, FaceAnalyzer { mood ->
                            if (!isMoodDetected) {
                                moodVotes[mood] = (moodVotes[mood] ?: 0) + 1
                                detectionCount++
                                if (detectionCount >= 10) {
                                    isMoodDetected = true
                                    val finalMood =
                                        moodVotes.maxByOrNull { it.value }?.key ?: "biasa"
                                    onMoodDetected(finalMood)
                                }
                            }
                        })
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this@FaceScanActivity,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("FaceScan", "Camera binding failed", e)
            }
        }
    }

    private fun onMoodDetected(mood: String) {
        runOnUiThread {
            val emoji = when (mood) {
                "semangat" -> "🔥"
                "capek"    -> "😴"
                "stres"    -> "😣"
                "bosan"    -> "😑"
                else       -> "😐"
            }
            val label = when (mood) {
                "semangat" -> "Semangat"
                "capek"    -> "Capek"
                "stres"    -> "Stres"
                "bosan"    -> "Bosan"
                else       -> "Biasa"
            }
            binding.tvResult.text    = emoji
            binding.tvMoodLabel.text = "Terdeteksi: $label"
            binding.tvStatus.text    = "Mood berhasil dideteksi!"
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent().apply {
                putExtra("detected_mood", mood)
            }
            setResult(RESULT_OK, intent)
            finish()
        }, 1500)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}