package com.vsloong.image.compressor.engine

import android.graphics.BitmapFactory
import com.vsloong.image.compressor.provider.IStreamProvider
import com.vsloong.image.compressor.utils.ImageUtil
import java.io.File
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


internal class WeChatEngine(
    private val streamProvider: IStreamProvider,
    private val outputDir: File,
    private val quality: Int,
) : ICompressEngine {

    override fun compress(): Result<File> {
        try {
            checkOutputDir(outputDir)

            val info = ImageUtil.info(streamProvider)

            val options = BitmapFactory.Options()
            options.inSampleSize = computeInSampleSize(
                srcWidth = info.width,
                srcHeight = info.height
            ).getOrThrow()

            val tagBitmap =
                BitmapFactory.decodeStream(streamProvider.openInputStream(), null, options)
                    ?: return Result.failure(Throwable("decode stream error"))

            return compressAndWriteToFile(
                bitmap = tagBitmap,
                type = info.type,
                orientation = info.orientation,
                quality = quality,
                outputDir = outputDir
            )
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    /**
     * in sample size: wechat strategy
     */
    private fun computeInSampleSize(srcWidth: Int, srcHeight: Int): Result<Int> {
        val paddedWidth = if (srcWidth % 2 == 0) srcWidth else srcWidth + 1
        val paddedHeight = if (srcHeight % 2 == 0) srcHeight else srcHeight + 1

        if (paddedWidth <= 0 || paddedHeight <= 0) {
            return Result.failure(
                Throwable(
                    "padded width or height is 0, paddedWidth=$paddedWidth, paddedHeight=$paddedHeight"
                )
            )
        }

        val longSide: Int = max(paddedWidth, paddedHeight)
        val shortSide: Int = min(paddedWidth, paddedHeight)

        val scale = (shortSide.toFloat() / longSide)
        val sampleSize = if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                1
            } else if (longSide < 4990) {
                2
            } else if (longSide in 4991..10239) {
                4
            } else {
                longSide / 1280
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (longSide / 1280 == 0) 1 else longSide / 1280
        } else {
            ceil(longSide / (1280.0 / scale)).toInt()
        }

        return Result.success(sampleSize)
    }


}