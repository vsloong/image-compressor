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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.vsloong.image.compressor.ui.theme.ImageCompressorTheme
import com.vsloong.image.compressor.utils.ImageUtil
import java.io.File
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val TAG = "image-compressor"

    private var inputBitmap = mutableStateOf<Bitmap?>(null)
    private var outputBitmap = mutableStateOf<Bitmap?>(null)

    private val inputFileInfo = mutableStateOf("")
    private val outputFileInfo = mutableStateOf("")

    private var mediaUri: Uri? = null
    private var pictureUri: Uri? = null

    private fun getOutputDir(): File {
        return cacheDir.resolve("output")
    }

    private fun getUriForFile(file: File): Uri {
        val applicationId = this.packageName
        return FileProvider.getUriForFile(this, "${applicationId}.provider", file)
    }

    private fun processUri(uri: Uri) {
        val info = ImageUtil.info(uri = uri, context = this)
        inputFileInfo.value = "\ntype=${info.type}" +
                "\nw*h=${info.width}*${info.height}" +
                "\nsize=${info.size / 1024} KB" +
                "\nname=${uri}"

        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let {
            val bitmap =
                BitmapFactory.decodeStream(inputStream, null, BitmapFactory.Options())
            inputBitmap.value = bitmap
        }

        inputStream?.close()

        Compressor.run(
            uri = uri,
            context = this,
            quality = 50,
            outputDir = getOutputDir(),
            maxWidth = 500,
            maxHeight = 500,
        ).onSuccess { output ->
            outputBitmap.value = BitmapFactory.decodeFile(output.absolutePath)

            val info = ImageUtil.info(output)
            outputFileInfo.value = "\ntype=${info.type}" +
                    "\nw*h=${info.width}*${info.height}" +
                    "\nsize=${info.size / 1024} KB" +
                    "\nname=${output.name}"
        }.onFailure {
            Log.e(TAG, "error=${it.message}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // pick media launcher
        val pickMediaLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                Log.e(TAG, "Pick Media: $uri")
                mediaUri = uri
                uri?.let {
                    processUri(uri)
                }
            }


        // take picture launcher
        val takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
                if (result) {
                    pictureUri?.let {
                        processUri(it)
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
                        onAlbumClick = {
                            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onTakePictureClick = {
                            val file = File(cacheDir, "${UUID.randomUUID()}.jpg")
                            val uri = getUriForFile(file)
                            pictureUri = uri
                            takePictureLauncher.launch(uri)
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
    onAlbumClick: () -> Unit = {},
    onTakePictureClick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { onAlbumClick.invoke() }) { Text(text = "Album") }
            Button(onClick = { onTakePictureClick.invoke() }) { Text(text = "TakePicture") }
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
                        contentScale = ContentScale.FillWidth,
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
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(text = outputFileInfo, color = Color.Black)
            }
        }
    }
}