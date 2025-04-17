package com.fpf.sentinellens.lib.ml

import kotlin.math.sqrt


infix fun FloatArray.dot(other: FloatArray) =
    foldIndexed(0.0) { i, acc, cur -> acc + cur * other[i] }.toFloat()

fun normalizeL2(inputArray: FloatArray): FloatArray {
    var norm = 0.0f
    for (i in inputArray.indices) {
        norm += inputArray[i] * inputArray[i]
    }
    norm = sqrt(norm)
    return inputArray.map { it / norm }.toFloatArray()
}

fun getSimilarities(embedding: FloatArray, comparisonEmbeddings: List<FloatArray>): List<Float> {
    return comparisonEmbeddings.map { embedding dot it }
}

fun dotProduct(a: FloatArray, b: FloatArray): Float {
    var result = 0.0f
    for (i in a.indices) {
        result += a[i] * b[i]
    }
    return result
}

fun getTopN(similarities: List<Float>, n: Int): List<Int> {
    return similarities.indices
        .sortedByDescending { similarities[it] }
        .take(n)
}