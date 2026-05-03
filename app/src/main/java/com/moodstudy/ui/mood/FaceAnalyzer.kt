package com.moodstudy.ui.mood

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val onMoodResult: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
    )

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]

                    val smileProb = face.smilingProbability ?: 0f
                    val leftEye  = face.leftEyeOpenProbability ?: 1f
                    val rightEye = face.rightEyeOpenProbability ?: 1f
                    val avgEye   = (leftEye + rightEye) / 2f

                    // Rule-based mood detection
                    val mood = when {
                        smileProb >= 0.75f                   -> "semangat"
                        smileProb >= 0.4f && avgEye >= 0.6f  -> "biasa"
                        avgEye < 0.4f                        -> "capek"
                        smileProb < 0.2f && avgEye < 0.6f   -> "stres"
                        else                                  -> "bosan"
                    }

                    onMoodResult(mood)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}