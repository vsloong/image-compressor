package com.vsloong.image.compressor.provider

import java.io.InputStream

interface IStreamProvider {

    fun openInputStream(): InputStream

    fun close()
}