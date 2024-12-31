package com.vsloong.image.compressor.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.vsloong.image.compressor.ImageData
import com.vsloong.image.compressor.provider.IStreamProvider
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
            type = getImageFileType(options.outMimeType),
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
            type = getImageFileType(options.outMimeType),
            size = inputStream.available().toLong()
        )
        return info
    }

    fun info(iStreamProvider: IStreamProvider): ImageData {
        val options = createOptions()
        val stream = iStreamProvider.openInputStream()
        BitmapFactory.decodeStream(stream, null, options)
        val size = stream.available().toLong()
        val imageType = getImageFileType(options.outMimeType)
        val orientation = if (imageType == ImageType.JPEG) {
            OrientationChecker.getOrientation(iStreamProvider.openInputStream())
        } else {
            0
        }

        val info = ImageData(
            width = options.outWidth,
            height = options.outHeight,
            type = getImageFileType(options.outMimeType),
            size = size,
            orientation = orientation
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
            type = getImageFileType(options.outMimeType),
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
            type = getImageFileType(options.outMimeType),
            size = file.length()
        )
        return info
    }

    private fun createOptions() = BitmapFactory.Options().apply {
        this.inJustDecodeBounds = true
        this.inSampleSize = 1
    }

    /**
     * get image format from BitmapFactory.Options.outMimeType
     */
    private fun getImageFileType(string: String?): ImageType {
        if (string == null) {
            return ImageType.JPEG
        }

        return when (string) {
            "image/png" -> ImageType.PNG
            "image/webp" -> ImageType.WEBP
            else -> ImageType.JPEG
        }
    }
}