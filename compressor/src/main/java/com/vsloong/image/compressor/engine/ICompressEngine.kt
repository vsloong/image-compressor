package com.vsloong.image.compressor.engine

import android.graphics.Bitmap
import android.graphics.Matrix
import com.vsloong.image.compressor.utils.ImageType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

interface ICompressEngine {

    fun compress(): Result<File>

    /**
     * check output dir exists or not
     */
    fun checkOutputDir(outputDir: File) {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        } else {
            if (!outputDir.isDirectory) {
                throw Throwable("output dir need be a directory!")
            }
        }
    }


    /**
     * compress bitmap and write to file
     */
    fun compressAndWriteToFile(
        bitmap: Bitmap,
        type: ImageType,
        orientation: Int,
        quality: Int,
        outputDir: File
    ): Result<File> {
        return try {

            // if need rotate
            val realBitmap = if (type is ImageType.JPEG && orientation != 0) {
                val matrix = Matrix()
                matrix.postRotate(orientation.toFloat())
                val rotatedBitmap =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                bitmap.recycle()
                rotatedBitmap
            } else {
                bitmap
            }

            ByteArrayOutputStream().use { stream ->
                realBitmap.compress(type.compressFormat, quality, stream)
                realBitmap.recycle()

                val outputFile = File(outputDir, "${System.currentTimeMillis()}.${type.extension}")

                FileOutputStream(outputFile).use { fos ->
                    fos.write(stream.toByteArray())
                    fos.flush()
                }
                Result.success(outputFile)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}