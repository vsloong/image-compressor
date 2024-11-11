package com.vsloong.image.compressor.provider

import java.io.InputStream

internal abstract class AStreamProvider : IStreamProvider {

    private var inputStream: InputStream? = null

    override fun openInputStream(): InputStream {
        close()
        inputStream = realOpenInputStream()
        return inputStream!!
    }

    override fun close() {
        if (inputStream != null) {
            try {
                inputStream?.close()
            } catch (_: Throwable) {

            } finally {
                inputStream = null
            }
        }
    }

    abstract fun realOpenInputStream(): InputStream
}