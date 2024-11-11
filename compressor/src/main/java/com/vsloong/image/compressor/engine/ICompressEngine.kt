package com.vsloong.image.compressor.engine

import android.graphics.Bitmap
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
     * get image format from BitmapFactory.Options.outMimeType
     */
    fun getFileFormat(string: String?): Bitmap.CompressFormat {
        if (string == null) {
            return Bitmap.CompressFormat.JPEG
        }

        return when (string) {
            "image/png" -> Bitmap.CompressFormat.PNG
            "image/webp" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }

    /**
     * compress bitmap and write to file
     */
    fun compressAndWriteToFile(
        bitmap: Bitmap,
        type: String,
        quality: Int,
        outputDir: File
    ): Result<File> {
        return try {
            ByteArrayOutputStream().use { stream ->
                bitmap.compress(getFileFormat(type), quality, stream)
                bitmap.recycle()

                val extension = type.replace("image/", "")
                val outputFile = File(outputDir, "${System.currentTimeMillis()}.$extension")

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