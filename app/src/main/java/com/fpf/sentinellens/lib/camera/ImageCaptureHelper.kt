package com.fpf.sentinellens.lib.camera

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class ImageCaptureHelper(
    private val context: Context,
    private val listener: ImageCaptureListener? = null
) {

    companion object {
        private const val TAG = "ImageCaptureHelper"
    }

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    /**
     * Starts the image capture process by opening the front camera and configuring the image reader.
     */
    fun startImageCapture() {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Get the first front camera id
            val frontCameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            } ?: run {
                listener?.onError(Exception("No front camera found"))
                return
            }

            // Get the maximum supported JPEG size from the camera characteristics
            val characteristics = cameraManager.getCameraCharacteristics(frontCameraId)
            val jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                ?.getOutputSizes(ImageFormat.JPEG)
            val chosenSize = jpegSizes?.maxByOrNull { it.width * it.height } ?: run {
                listener?.onError(Exception("Unable to determine image size"))
                return
            }

            Log.d(TAG, "Chosen image size: ${chosenSize.width} x ${chosenSize.height}")

            // Initialize the ImageReader for the chosen size and format
            imageReader = ImageReader.newInstance(chosenSize.width, chosenSize.height, ImageFormat.JPEG, 1)
            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()
                image?.use {
                    if (it.format == ImageFormat.JPEG && it.width > 0 && it.height > 0) {
                        val buffer: ByteBuffer = it.planes[0].buffer
                        val bytes = ByteArray(buffer.remaining())
                        buffer.get(bytes)
                        if (isValidJpeg(bytes)) {
                            saveImage(bytes)
                        } else {
                            listener?.onError(Exception("Invalid JPEG header, image skipped"))
                        }
                    } else {
                        listener?.onError(Exception("Invalid image dimensions or format"))
                    }
                }
                closeCamera()
            }, Handler(Looper.getMainLooper()))

            // Check for CAMERA permission before opening the camera
            if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        cameraDevice = camera
                        createCaptureSession()
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                        listener?.onError(Exception("Camera disconnected"))
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        camera.close()
                        listener?.onError(Exception("Camera error: $error"))
                    }
                }, Handler(Looper.getMainLooper()))
            } else {
                listener?.onError(Exception("Camera permission not granted"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Camera start error", e)
            listener?.onError(e)
        }
    }

    /**
     * Creates a camera capture session for taking a still image.
     */
    private fun createCaptureSession() {
        try {
            val surface = imageReader?.surface ?: throw Exception("ImageReader surface is null")
            cameraDevice?.createCaptureSession(
                listOf(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        try {
                            val captureRequest = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                                addTarget(surface)
                                set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                            }
                            session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(
                                    session: CameraCaptureSession,
                                    request: CaptureRequest,
                                    result: TotalCaptureResult
                                ) {
                                    Log.d(TAG, "Image capture completed")
                                }
                            }, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error during capture session configuration", e)
                            listener?.onError(e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        listener?.onError(Exception("Capture session configuration failed"))
                    }
                },
                Handler(Looper.getMainLooper())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating capture session", e)
            listener?.onError(e)
        }
    }

    /**
     * Saves the captured JPEG byte array to MediaStore.
     */
    private fun saveImage(bytes: ByteArray) {
        try {
            val filename = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val resolver = context.contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(bytes)
                    stream.flush()
                }
                Log.d(TAG, "Image saved: $uri")
                listener?.onImageCaptured(uri)
            } else {
                listener?.onError(Exception("MediaStore URI is null"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Save image error", e)
            listener?.onError(e)
        }
    }

    /**
     * Checks the validity of the JPEG header.
     */
    private fun isValidJpeg(bytes: ByteArray): Boolean {
        return bytes.isNotEmpty() && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
    }

    /**
     * Closes the camera device, capture session, and image reader.
     */
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

        try {
            imageReader?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing image reader", e)
        }
        imageReader = null
    }
}


interface ImageCaptureListener {
    fun onImageCaptured(imageUri: Uri)
    fun onError(exception: Exception)
}
