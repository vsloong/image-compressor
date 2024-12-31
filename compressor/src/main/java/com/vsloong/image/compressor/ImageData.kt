package com.vsloong.image.compressor

import com.vsloong.image.compressor.utils.ImageType

data class ImageData(
    val width: Int,
    val height: Int,
    val size: Long,
    val type: ImageType,
    val orientation: Int = 0,
)
