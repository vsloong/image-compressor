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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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

    private val TAG = "image-compressor"

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

        // copy assets res to local
        AssetsUtils.copyAssetsToCache(this)?.listFiles()?.filter { !it.isDirectory }
            ?.let { queue.addAll(it) }

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                Log.e(TAG, "Pick Media: $uri")
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

                    Compressor.run(
                        uri = uri, context = this, quality = 50, outputDir = outputDir,
                        maxWidth = 500,
                        maxHeight = 500,
                    )
                        .onSuccess { output ->
                            outputBitmap.value = BitmapFactory.decodeFile(output.absolutePath)

                            val info = ImageUtil.info(output)
                            outputFileInfo.value = "name=${output.name}" +
                                    "\ntype=${info.type}" +
                                    "\nwidth*height=${info.width}x${info.height}" +
                                    "\nsize=${info.size / 1024} KB"
                        }.onFailure {
                            Log.e(TAG, "error=${it.message}")
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
                                inputFileInfo.value = "get image info error, file=${file.name}"
                                return@ImageCompressScreen
                            }

                            inputBitmap.value = BitmapFactory.decodeFile(file.absolutePath)
                            inputFileInfo.value = "name=${file.name}" +
                                    "\ntype=${info.type}" +
                                    "\nwidth*height=${info.width}x${info.height}" +
                                    "\nsize=${info.size / 1024} KB"
                        },
                        onCompressClick = {
                            val input = currentFile ?: return@ImageCompressScreen

                            Compressor.run(
                                file = input, outputDir = outputDir, quality = 50,
                                maxWidth = 500,
                                maxHeight = 500,
                            ).onSuccess { output ->
                                outputBitmap.value =
                                    BitmapFactory.decodeFile(output.absolutePath)

                                val info = ImageUtil.info(output)
                                outputFileInfo.value = "name=${output.name}" +
                                        "\ntype=${info.type}" +
                                        "\nwidth*height=${info.width}x${info.height}" +
                                        "\nsize=${info.size / 1024} KB"
                            }.onFailure {
                                Log.e(TAG, "error=${it.message}")
                            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageCompressScreen(
    modifier: Modifier,
    srcBitmap: Bitmap?,
    inputFileInfo: String,
    compressedBitmap: Bitmap?,
    outputFileInfo: String,
    onNextClick: () -> Unit = {},
    onCompressClick: () -> Unit = {},
    onAlbumClick: () -> Unit = {},
) {
    Column(modifier = modifier) {
        FlowRow {
            Button(onClick = { onNextClick.invoke() }) { Text(text = "Next") }
            Button(onClick = { onCompressClick.invoke() }) { Text(text = "Compress") }
            Button(onClick = { onAlbumClick.invoke() }) { Text(text = "Album") }
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