package com.fpf.sentinellens.lib.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.max
import kotlin.math.min

fun cropFaces(bitmap: Bitmap, boxes: List<FloatArray>): List<Bitmap> {
    val faces = mutableListOf<Bitmap>()
    for (box in boxes) {
        val x1 = max(0, box[0].toInt())
        val y1 = max(0, box[1].toInt())
        val x2 = min(bitmap.width, box[2].toInt())
        val y2 = min(bitmap.height, box[3].toInt())
        val width = x2 - x1
        val height = y2 - y1
        if (width > 0 && height > 0) {
            val faceBitmap = Bitmap.createBitmap(bitmap, x1, y1, width, height)
            faces.add(faceBitmap)
        }
    }
    return faces
}


fun drawBoxes(bitmap: Bitmap, boxes: List<FloatArray>, color: Int, strokeWidth: Float = 2f): Bitmap {
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    val paint = Paint().apply {
        this.color = color
        this.strokeWidth = strokeWidth
    }

    for (box in boxes) {
        val x1 = max(0, box[0].toInt())
        val y1 = max(0, box[1].toInt())
        val x2 = min(mutableBitmap.width, box[2].toInt())
        val y2 = min(mutableBitmap.height, box[3].toInt())

        canvas.drawRect(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint)
    }

    return mutableBitmap
}


/**
 * Applies Non-Maximum Suppression (NMS) over the list of boxes.
 * This prevents mutliple similar bounding boxes
 *
 * @param boxes A list of boxes, where each box is represented as FloatArray of four values: [x1, y1, x2, y2].
 * @param scores A list of confidence scores corresponding to each box.
 * @param iouThreshold The threshold for Intersection-over-Union.
 * @return A list of indices corresponding to boxes that are kept after NMS.
 */
fun nms(boxes: List<FloatArray>, scores: List<Float>, iouThreshold: Float): List<Int> {
    if (boxes.isEmpty()) return emptyList()

    val indices = scores.indices.sortedByDescending { scores[it] }.toMutableList()
    val keep = mutableListOf<Int>()

    while (indices.isNotEmpty()) {
        val current = indices.removeAt(0)
        keep.add(current)
        val currentBox = boxes[current]

        indices.removeAll { idx ->
            val iou = computeIoU(currentBox, boxes[idx])
            iou > iouThreshold
        }
    }
    return keep
}

/**
 * Computes the Intersection over Union (IoU) of two boxes.
 *
 * @param boxA A box in the format [x1, y1, x2, y2].
 * @param boxB A box in the format [x1, y1, x2, y2].
 * @return IoU value.
 */
fun computeIoU(boxA: FloatArray, boxB: FloatArray): Float {
    val x1 = max(boxA[0], boxB[0])
    val y1 = max(boxA[1], boxB[1])
    val x2 = min(boxA[2], boxB[2])
    val y2 = min(boxA[3], boxB[3])
    val intersectionArea = max(0f, x2 - x1) * max(0f, y2 - y1)
    val areaA = max(0f, boxA[2] - boxA[0]) * max(0f, boxA[3] - boxA[1])
    val areaB = max(0f, boxB[2] - boxB[0]) * max(0f, boxB[3] - boxB[1])
    val unionArea = areaA + areaB - intersectionArea
    return if (unionArea <= 0f) 0f else intersectionArea / unionArea
}