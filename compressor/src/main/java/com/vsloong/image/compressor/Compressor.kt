package com.vsloong.image.compressor

import android.content.Context
import android.net.Uri
import com.vsloong.image.compressor.engine.WeChatEngine
import com.vsloong.image.compressor.provider.File2StreamProvider
import com.vsloong.image.compressor.provider.Uri2StreamProvider
import java.io.File

object Compressor {

    fun run(uri: Uri, context: Context, quality: Int = 75, outputDir: File) =
        kotlin.runCatching {
            WeChatEngine(
                streamProvider = Uri2StreamProvider(uri = uri, context = context),
                outputDir = outputDir,
                quality = quality,
            ).compress().getOrThrow()
        }

    fun run(file: File, quality: Int = 75, outputDir: File) =
        kotlin.runCatching {
            WeChatEngine(
                streamProvider = File2StreamProvider(file = file),
                outputDir = outputDir,
                quality = quality
            ).compress().getOrThrow()
        }

}