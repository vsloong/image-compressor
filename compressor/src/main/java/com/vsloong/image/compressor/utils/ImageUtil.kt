package com.vsloong.image.compressor.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.vsloong.image.compressor.ImageData
import java.io.File
import java.io.InputStream

object ImageUtil {

    fun info(uri: Uri, context: Context): ImageData {
        val options = createOptions()
        val stream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(stream, null, options)
        val info = ImageData(
            width = options.outWidth,
            height = options.outHeight,
            type = options.outMimeType,
            size = stream?.available()?.toLong() ?: 0L
        )
        stream?.close()
        return info
    }

    fun info(inputStream: InputStream): ImageData {
        val options = createOptions()
        BitmapFactory.decodeStream(inputStream, null, options)
        val info = ImageData(
            width = options.outWidth,
            height = options.outHeight,
            type = options.outMimeType,
            size = inputStream.available().toLong()
        )
        return info
    }

    fun info(path: String): ImageData {
        val options = createOptions()
        BitmapFactory.decodeFile(path, options)
        val file = File(path)
        val info = ImageData(
            width = options.outWidth,
            height = options.outHeight,
            type = options.outMimeType,
            size = file.length()
        )
        return info
    }

    fun info(file: File): ImageData {
        val options = createOptions()
        BitmapFactory.decodeFile(file.absolutePath, options)
        val info = ImageData(
            width = options.outWidth,
            height = options.outHeight,
            type = options.outMimeType,
            size = file.length()
        )
        return info
    }

    private fun createOptions() = BitmapFactory.Options().apply {
        this.inJustDecodeBounds = true
        this.inSampleSize = 1
    }
}