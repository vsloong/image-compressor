package com.vsloong.image.compressor

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object AssetsUtils {
    private const val TAG = "AssetCopyUtil"

    fun sampleCacheDir(context: Context) = File(context.cacheDir, "samples")

    fun copyAssetsToCache(context: Context) :File?{
        val assetManager = context.assets
        val cacheDirectory = sampleCacheDir(context)

        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }

        try {
            val files = assetManager.list("samples")
            files?.forEach { filename ->
                Log.e(TAG, "copyAssetsToCache: filename=$filename")
                val outFile = File(cacheDirectory, filename)
                if (!outFile.exists()) {
                    copyFile(assetManager, filename, outFile)
                }
            }
            return cacheDirectory
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy assets to cache directory", e)
            return null
        }
    }

    private fun copyFile(assetManager: AssetManager, filename: String, outFile: File) {
        var inStream: InputStream? = null
        var outStream: OutputStream? = null
        try {
            inStream = assetManager.open(filename)
            outStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                outStream.write(buffer, 0, read)
            }
            outStream.flush()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy file $filename", e)
        } finally {
            inStream?.close()
            outStream?.close()
        }
    }
}