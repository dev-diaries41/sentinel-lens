package com.fpf.sentinellens.data.faces

import androidx.room.*

class Converters {
    @TypeConverter
    fun fromFloatArray(value: FloatArray): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        return if (value.isEmpty()) floatArrayOf() else value.split(",").map { it.toFloat() }.toFloatArray()
    }

    @TypeConverter
    fun fromImageType(type: FaceType): String {
        return type.name
    }

    @TypeConverter
    fun toImageType(type: String): FaceType {
        return FaceType.valueOf(type)
    }
}


