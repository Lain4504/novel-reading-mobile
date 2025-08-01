package indi.dmzz_yyhyy.lightnovelreader.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {

    fun getImageSize(imageUrl: String?): Size? {
        var connection: HttpURLConnection? = null
        try {
            val requestUrl = URL(imageUrl)
            connection = requestUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val inputStream = connection.inputStream
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                return Size(options.outWidth, options.outHeight)
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return null
    }

    fun urlToBitmap(
        scope: CoroutineScope,
        imageURL: String,
        context: Context,
        onSuccess: (Bitmap) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageURL)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)

                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            onSuccess(bitmap)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onError(Throwable("Failed to cast drawable to BitmapDrawable"))
                        }
                    }
                } else if (result is ErrorResult) {
                    throw result.throwable
                } else {
                    throw Throwable("Unknown result type")
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    suspend fun saveBitmapAsPng(
        context: Context,
        bitmap: Bitmap
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "${System.currentTimeMillis()}.png"
                val mimeType = "image/png"

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/LightNovelReader"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext null

                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                fileName
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}