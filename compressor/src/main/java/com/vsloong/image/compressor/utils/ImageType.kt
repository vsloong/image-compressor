package com.vsloong.image.compressor.utils

import android.graphics.Bitmap.CompressFormat

sealed class ImageType(val extension: String, val compressFormat: CompressFormat) {
    data object JPEG : ImageType(extension = "jpeg", CompressFormat.JPEG)
    data object PNG : ImageType(extension = "png", CompressFormat.PNG)
    data object WEBP : ImageType(extension = "webp", CompressFormat.WEBP)
}