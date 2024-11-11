package com.vsloong.image.compressor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.vsloong.image.compressor.ui.theme.ImageCompressorTheme
import com.vsloong.image.compressor.utils.ImageUtil
import java.io.File
import java.util.LinkedList

class MainActivity : ComponentActivity() {

    private val TAG = "图片压缩"

    private val queue: LinkedList<File> = LinkedList()


    private var inputBitmap = mutableStateOf<Bitmap?>(null)
    private var outputBitmap = mutableStateOf<Bitmap?>(null)

    private val inputFileInfo = mutableStateOf("")
    private val outputFileInfo = mutableStateOf("")

    private var currentFile: File? = null

    private var mediaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val outputDir = cacheDir.resolve("output")


        cacheDir.listFiles()?.let { queue.addAll(it) }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                Log.e(TAG, "选择相册:$uri")
                mediaUri = uri
                uri?.let {

                    val info = ImageUtil.info(uri = uri, context = this)
                    inputFileInfo.value = "name=${uri}" +
                            "\ntype=${info.type}" +
                            "\nwidth*height=${info.width}x${info.height}" +
                            "\nsize=${info.size / 1024} KB"

                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.let {
                        val bitmap =
                            BitmapFactory.decodeStream(inputStream, null, BitmapFactory.Options())
                        inputBitmap.value = bitmap
                    }

                    inputStream?.close()

                    Compressor.run(uri = uri, context = this, quality = 50, outputDir = outputDir)
                        .onSuccess { output ->
                            outputBitmap.value = BitmapFactory.decodeFile(output.absolutePath)

                            val info = ImageUtil.info(output)
                            outputFileInfo.value = "name=${output.name}" +
                                    "\ntype=${info.type}" +
                                    "\nwidth*height=${info.width}x${info.height}" +
                                    "\nsize=${info.size / 1024} KB"
                        }.onFailure {
                            Log.e(TAG, "压缩失败：${it.message}")
                        }
                }

            }

        setContent {
            ImageCompressorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageCompressScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        srcBitmap = inputBitmap.value,
                        inputFileInfo = inputFileInfo.value,
                        compressedBitmap = outputBitmap.value,
                        outputFileInfo = outputFileInfo.value,
                        onNextClick = {
                            val file = queue.poll() ?: return@ImageCompressScreen

                            currentFile = file
                            val info = ImageUtil.info(file)
                            if (info.width <= 0 || info.height <= 0) {
                                inputFileInfo.value = "获取图片信息失败：${file.name}"
                                return@ImageCompressScreen
                            }

                            inputBitmap.value = BitmapFactory.decodeFile(file.absolutePath)
                            val input = inputBitmap.value ?: return@ImageCompressScreen

                            inputFileInfo.value = "name=${file.name}" +
                                    "\ntype=${info.type}" +
                                    "\nwidth*height=${info.width}x${info.height}" +
                                    "\nsize=${info.size / 1024} KB"
                        },
                        onCompressClick = {
                            val input = currentFile ?: return@ImageCompressScreen

                            Compressor.run(file = input, outputDir = outputDir, quality = 50)
                                .onSuccess { output ->
                                    outputBitmap.value =
                                        BitmapFactory.decodeFile(output.absolutePath)

                                    val info = ImageUtil.info(output)
                                    outputFileInfo.value = "name=${output.name}" +
                                            "\ntype=${info.type}" +
                                            "\nwidth*height=${info.width}x${info.height}" +
                                            "\nsize=${info.size / 1024} KB"
                                }
                                .onFailure {
                                    Log.e(TAG, "压缩失败：${it.message}")
                                }
                        },
                        onCopyClick = {
                            AssetsUtils.copyAssetsToCache(this)
                            cacheDir.listFiles()?.let { queue.addAll(it) }
                        },
                        onAlbumClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ImageCompressScreen(
    modifier: Modifier,
    srcBitmap: Bitmap?,
    inputFileInfo: String,
    compressedBitmap: Bitmap?,
    outputFileInfo: String,
    onCopyClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onCompressClick: () -> Unit = {},
    onAlbumClick: () -> Unit = {},
) {
    Column(modifier = modifier) {

        Row {
            Button(onClick = { onCopyClick.invoke() }) { Text(text = "拷贝到本地") }
            Button(onClick = { onNextClick.invoke() }) { Text(text = "下一张") }
            Button(onClick = { onCompressClick.invoke() }) { Text(text = "开始压缩") }
            Button(onClick = { onAlbumClick.invoke() }) { Text(text = "打开相册") }
        }

        Row {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                if (srcBitmap != null) {
                    Image(
                        bitmap = srcBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(text = inputFileInfo, color = Color.Black)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            ) {
                if (compressedBitmap != null) {
                    Image(
                        bitmap = compressedBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(text = outputFileInfo, color = Color.Black)
            }
        }
    }
}