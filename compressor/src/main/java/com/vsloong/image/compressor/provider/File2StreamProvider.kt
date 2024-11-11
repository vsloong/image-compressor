package com.vsloong.image.compressor.provider

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

internal class File2StreamProvider(private val file: File) : AStreamProvider() {
    override fun realOpenInputStream(): InputStream {
        return FileInputStream(file)
    }
}