package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        // Resource URI and Blur Level
        val resourceUri = inputData.getString(KEY_IMAGE_URI)
        val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

        // Show Notif to show our work is in progress
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            // Add delay to emulate long running task
            delay(DELAY_TIME_MILLIS)

            return@withContext try {
                // Check if input URI is Populated
                require (!resourceUri.isNullOrBlank()) {
                    val errorMessage =
                        applicationContext.resources.getString(R.string.invalid_input_uri)
                    Log.e(TAG, errorMessage)
                    errorMessage
                }

                // Log Input URI
                Log.d(TAG, "Input URI: $resourceUri")

                // Content Resolver
                val resolver = applicationContext.contentResolver

                // Decode Resource
//                val picture = BitmapFactory.decodeResource(
//                    applicationContext.resources,
//                    R.drawable.android_cupcake
//                )
                // Decode from URI
                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )

                // Blur with blur level 1
//                val output = blurBitmap(picture, 1)
                // BLur with blur level provided
                val output = blurBitmap(picture, blurLevel)

                // Write bitmap to a temp file
                val outputUri = writeBitmapToFile(applicationContext, output)

                // Convert URI to Data
                val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

                // Show Notif Output URI
//                makeStatusNotification(
//                    "Output is $outputUri",
//                    applicationContext
//                )

                // Log Output URI
                Log.d(TAG, "Output URI: $outputUri")

                Result.success(outputData)
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )
                Result.failure()
            }
        }

    }

}