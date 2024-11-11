package com.vsloong.image.compressor.provider

import android.content.Context
import android.net.Uri
import com.vsloong.image.compressor.provider.AStreamProvider
import java.io.InputStream

internal class Uri2StreamProvider(
    private val uri: Uri,
    private val context: Context,
) : AStreamProvider() {
    override fun realOpenInputStream(): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw Throwable("uri open input stream return null")
    }
}