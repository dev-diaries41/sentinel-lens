package com.fpf.sentinellens.lib.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VideoCaptureHelper(
    private val context: Context,
    private val listener: IVideoCaptureListener? = null,
    private val isFrameProcessingActive: Boolean = false,
    private val frameInterval: Long = 1000L,
    private val maxDuration: Long? = null,
    private val lensFacing: Int = CameraCharacteristics.LENS_FACING_BACK,
    private var previewSurface: android.view.Surface? = null
) {

    companion object {
        private const val TAG = "VideoCaptureHelper"
        private const val THREAD_SHUTDOWN_DELAY = 1000L
        private const val VIDEO_WIDTH = 1920
        private const val VIDEO_HEIGHT = 1080
        private const val FRAME_RATE = 30
        private const val BIT_RATE = 10_000_000
    }

    private val frameProcessingJob = SupervisorJob()
    private val frameProcessingScope = CoroutineScope(Dispatchers.Default + frameProcessingJob)
    val orientation = getOrientation(lensFacing)?: 0

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var mediaRecorder: MediaRecorder
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var videoFilePath: String? = null
    private var imageReader: ImageReader? = null

    fun startCameraAndRecording() {
        startBackgroundThread()
        setupMediaRecorder()
        if (isFrameProcessingActive) {
            setupImageReader()
        }
        openCamera(lensFacing)
    }

    fun setPreviewSurface(surface: android.view.Surface) {
        previewSurface = surface
    }

    private fun setupImageReader() {
        // onFrame() takes ~100ms to process.
        // At 30 FPS, a new frame arrives every ~33ms.
        // 100 / 33 ≈ 3, meaning up to 3 frames could be in the pipeline at once.
        // So we set maxImages = 3 to avoid buffer acquisition errors.

        val maxImages = 3
        imageReader = ImageReader.newInstance(VIDEO_WIDTH, VIDEO_HEIGHT, android.graphics.ImageFormat.YUV_420_888, maxImages)

        var lastFrameTime = 0L

        imageReader?.setOnImageAvailableListener({ reader ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastFrameTime >= frameInterval) {
                val image = reader.acquireLatestImage()
                if (image != null) {
                    lastFrameTime = currentTime
                    frameProcessingScope.launch {
                        try {
                            listener?.onFrame(image, orientation)
                        } finally {
                            image.close()
                        }
                    }
                }
            } else {
                reader.acquireLatestImage()?.close()
            }
        }, backgroundHandler)
    }


    private fun setupMediaRecorder() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        try {
            mediaRecorder.apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoEncodingBitRate(BIT_RATE)
                setVideoFrameRate(FRAME_RATE)
                setOrientationHint(orientation)
                setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT)

                val videoFile = createVideoFile()
                videoFilePath = videoFile.absolutePath

                setOutputFile(videoFile.absolutePath)
                prepare()
            }
        } catch (e: IOException) {
            Log.e(TAG, "MediaRecorder preparation failed", e)
            listener?.onError(e)
        }
    }

    private fun getOrientation(lensFacing: Int): Int?{
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(CameraCharacteristics.LENS_FACING) == lensFacing
        }

        if (cameraId != null) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        }
        return null
    }

    private fun openCamera(lensFacing: Int) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == lensFacing
            } ?: run {
                val facing = if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) "front" else "back"
                Log.e(TAG, "No $facing camera found")
                listener?.onError(Exception("No $facing camera found"))
                return
            }

            if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        createCaptureSession()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.e(TAG, "Camera disconnected")
                        camera.close()
                        listener?.onError(Exception("Camera disconnected"))
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.e(TAG, "Camera error: $error")
                        camera.close()
                        listener?.onError(Exception("Camera error: $error"))
                    }
                }, backgroundHandler)
            } else {
                val error = Exception("CAMERA permission not granted")
                Log.e(TAG, error.message ?: "")
                listener?.onError(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            listener?.onError(e)
        }
    }

    private fun createCaptureSession() {
        try {
            val surfaces = mutableListOf<android.view.Surface>()

            surfaces.add(mediaRecorder.surface)

            // If image streaming is enabled, add the ImageReader surface.
            imageReader?.surface?.let { surfaces.add(it) }

            // Optionally allow displaying camera session in UI.
            previewSurface?.let { surfaces.add(it) }

            cameraDevice?.createCaptureSession(
                surfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            val captureRequestBuilder = cameraDevice!!
                                .createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                                .apply {
                                    surfaces.forEach { surface ->
                                        addTarget(surface)
                                    }
                                    set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                                }
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)

                                mediaRecorder.start()
                                Log.d(TAG, "Video recording started")
                                listener?.onRecordingStarted()

                                if(maxDuration != null){
                                    backgroundHandler?.postDelayed({
                                        stopVideoCapture()
                                    }, maxDuration)
                                }

                        } catch (e: Exception) {
                            Log.e(TAG, "Error during capture session configuration", e)
                            listener?.onError(e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "Capture session configuration failed")
                        listener?.onError(Exception("Capture session configuration failed"))
                    }
                },
                backgroundHandler
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating capture session", e)
            listener?.onError(e)
        }
    }

    fun stopVideoCapture() {
        try {
            captureSession?.stopRepeating()
            captureSession?.abortCaptures()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capture session", e)
        }

        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            Log.d(TAG, "Video recording stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media recorder", e)
            listener?.onError(e)
        }

        if (isFrameProcessingActive) {
            imageReader?.close()
            imageReader = null
            Log.d(TAG, "streaming session stopped")
        }

        closeCamera()

        Handler(Looper.getMainLooper()).post {
            listener?.onRecordingStopped(videoFilePath)
        }

        // Optionally, delay shutdown of background thread.
        Handler(Looper.getMainLooper()).postDelayed({
            stopBackgroundThread()
            frameProcessingScope.cancel()
        }, THREAD_SHUTDOWN_DELAY)
    }

    private fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "VID_$timestamp.mp4"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (directory != null && !directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, fileName)
    }

    private fun closeCamera() {
        try {
            captureSession?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing capture session", e)
        }
        captureSession = null

        try {
            cameraDevice?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing camera device", e)
        }
        cameraDevice = null
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("VideoCameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
        Log.d(TAG, "Background thread started")
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            Log.d(TAG, "Background thread stopped")
        } catch (e: InterruptedException) {
            Log.e(TAG, "Error stopping background thread", e)
        }
        backgroundThread = null
        backgroundHandler = null
    }
}

interface IVideoCaptureListener {
    fun onRecordingStarted()
    fun onRecordingStopped(videoFilePath: String?)
    fun onError(exception: Exception)
    suspend fun onFrame(image: Image, orientation: Int)
}
