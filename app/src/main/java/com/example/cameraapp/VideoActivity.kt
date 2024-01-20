package com.example.cameraapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import com.example.cameraapp.databinding.ActivityVideoBinding
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoBinding
    private var recording: Recording? = null
    private lateinit var cameraProviderListener: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var videoCapture: VideoCapture<Recorder>? = null
    private lateinit var imageCaptureExecutor: ExecutorService
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    binding.root,
                    "The camera permission is necessary",
                    Snackbar.LENGTH_INDEFINITE
                ).show()

            }
        }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        cameraProviderListener.addListener({
            val cameraProvider = cameraProviderListener.get()

            val recorder: Recorder = Recorder.Builder()
                .setExecutor(imageCaptureExecutor)
                .build()

            videoCapture = VideoCapture.Builder<Recorder>(recorder).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (e: Exception) {
                Log.d("TAG", "Use case binding failed")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderListener = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        imageCaptureExecutor = Executors.newSingleThreadExecutor()

        cameraPermissionResult.launch(Manifest.permission.CAMERA)

        binding.videoCaptureBtn.setOnClickListener {
            captureVideo()
        }

        binding.toPhotoBtn.setOnClickListener {
            startActivity(Intent(this, PhotoActivity::class.java))
        }

        binding.switchBtn.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            startCamera()
        }
        binding.galleryBtn.setOnClickListener {
            val intent = Intent(this, GalleryActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        imageCaptureExecutor.shutdown()
    }

    private fun captureVideo() {
        if (recording != null) {
            recording?.stop()
            binding.videoCaptureBtn.setBackgroundColor(getColor(R.color.green))
            recording = null
            return
        } else {
            binding.videoCaptureBtn.setBackgroundColor(getColor(R.color.red))
        }
        val fileName = "VIDEO_${System.currentTimeMillis()}.mp4"
        val file = File(externalMediaDirs[0], fileName)
        val options = FileOutputOptions.Builder(file).build()

        recording =
            videoCapture!!.output.prepareRecording(this, options)
                .start(
                    ContextCompat.getMainExecutor(this)
                ) { videoRecordEvent: VideoRecordEvent? ->
                    if (videoRecordEvent is VideoRecordEvent.Start) {
                        binding.videoCaptureBtn.isEnabled = true
                    } else if (videoRecordEvent is VideoRecordEvent.Finalize) {
                        if (!videoRecordEvent.hasError()) {
                            val msg =
                                "Video capture succeeded: " + videoRecordEvent.outputResults.outputUri
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        } else {
                            recording?.close()
                            recording = null
                            val msg = "Error: " + videoRecordEvent.error
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        }
                        binding.videoCaptureBtn.setBackgroundColor(getColor(R.color.green))
                    }
                }
    }
}