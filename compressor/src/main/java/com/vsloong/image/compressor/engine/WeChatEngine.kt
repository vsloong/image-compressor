package com.vsloong.image.compressor.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.vsloong.image.compressor.provider.IStreamProvider
import com.vsloong.image.compressor.utils.ImageUtil
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


/**
 * 仿微信压缩策略的引擎
 */
internal class WeChatEngine(
    private val streamProvider: IStreamProvider,
    private val outputDir: File,
    private val quality: Int,
) : ICompressEngine {

    override fun compress(): Result<File> {
        try {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            } else {
                if (!outputDir.isDirectory) {
                    return Result.failure(Throwable("output dir need be a directory!"))
                }
            }

            val info = ImageUtil.info(streamProvider.openInputStream())

            val options = BitmapFactory.Options()
            options.inSampleSize = computeInSampleSize(
                srcWidth = info.width,
                srcHeight = info.height
            ).getOrThrow()

            val tagBitmap =
                BitmapFactory.decodeStream(streamProvider.openInputStream(), null, options)
                    ?: return Result.failure(Throwable("decode stream error"))
            val stream = ByteArrayOutputStream()
            tagBitmap.compress(getFileFormat(info.type), quality, stream)
            tagBitmap.recycle()

            // 输出文件夹及文件、后缀等处理
            val extension = info.type.replace("image/", "")
            val outputFile = File(outputDir, "${System.currentTimeMillis()}.${extension}")

            val fos = FileOutputStream(outputFile)
            fos.write(stream.toByteArray())
            fos.flush()
            fos.close()
            stream.close()
            return Result.success(outputFile)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    /**
     * 采样策略：仿微信
     */
    override fun computeInSampleSize(srcWidth: Int, srcHeight: Int): Result<Int> {
        // 填充为偶数
        val paddedWidth = if (srcWidth % 2 == 0) srcWidth else srcWidth + 1
        val paddedHeight = if (srcHeight % 2 == 0) srcHeight else srcHeight + 1

        if (paddedWidth <= 0 || paddedHeight <= 0) {
            return Result.failure(
                Throwable(
                    "padded width or height is 0, paddedWidth=$paddedWidth, paddedHeight=$paddedHeight"
                )
            )
        }

        // 获取长边和短边
        val longSide: Int = max(paddedWidth, paddedHeight)
        val shortSide: Int = min(paddedWidth, paddedHeight)

        // 图片的长短比例系数
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
            // 此时图片是长图，向上取整
            ceil(longSide / (1280.0 / scale)).toInt()
        }

        return Result.success(sampleSize)
    }


    private fun getFileFormat(string: String?): Bitmap.CompressFormat {
        if (string == null) {
            return Bitmap.CompressFormat.JPEG
        }

        return when (string) {
            "image/png" -> Bitmap.CompressFormat.PNG
            "image/webp" -> Bitmap.CompressFormat.WEBP
            else -> Bitmap.CompressFormat.JPEG
        }
    }
}