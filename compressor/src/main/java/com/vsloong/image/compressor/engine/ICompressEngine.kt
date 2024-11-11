package com.vsloong.image.compressor.engine

import java.io.File

interface ICompressEngine {

    fun computeInSampleSize(srcWidth: Int, srcHeight: Int): Result<Int>

    fun compress(): Result<File>
}