package com.vsloong.image.compressor.provider

import java.io.InputStream

internal interface IStreamProvider {

    fun openInputStream(): InputStream

    fun close()
}