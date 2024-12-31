package com.vsloong.image.compressor.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.graphics.Matrix
import android.util.Log
import com.vsloong.image.compressor.provider.IStreamProvider
import com.vsloong.image.compressor.utils.ImageUtil
import java.io.File
import kotlin.math.min

internal class TargetSizeEngine(
    private val streamProvider: IStreamProvider,
    private val outputDir: File,
    private val quality: Int,
    private val maxWidth: Int,
    private val maxHeight: Int,
) : ICompressEngine {

    override fun compress(): Result<File> {
        try {
            checkOutputDir(outputDir)

            val info = ImageUtil.info(streamProvider)

            val widthScale = maxWidth * 1f / info.width
            val heightScale = maxHeight * 1f / info.height

            val tagBitmap =
                BitmapFactory.decodeStream(streamProvider.openInputStream(), null, Options())
                    ?: return Result.failure(Throwable("decode stream error"))


            val realScale = min(widthScale, heightScale)
            val targetBitmap = if (realScale < 1f) {
                val matrix = Matrix().apply { setScale(realScale, realScale) }
                val matrixBitmap =
                    Bitmap.createBitmap(tagBitmap, 0, 0, info.width, info.height, matrix, true)
                tagBitmap.recycle()
                matrixBitmap
            } else {
                tagBitmap
            }

            return compressAndWriteToFile(
                bitmap = targetBitmap,
                type = info.type,
                orientation = info.orientation,
                quality = quality,
                outputDir = outputDir
            )
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }


}