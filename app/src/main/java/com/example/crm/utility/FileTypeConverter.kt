package com.example.crm.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream


class FileTypeConverter {

        @TypeConverter
        fun toByteArray(bitmap: Bitmap): ByteArray {
            val resized = Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * 0.8).toInt(),
                (bitmap.height * 0.8).toInt(), true
            )
            val stream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }

        @TypeConverter
        fun toBitmap(byteArray: ByteArray): Bitmap {
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }

    fun toUri(context: Context, byteArray: ByteArray?): Uri? {
        if (byteArray == null) return null

        val bitmap = toBitmap(byteArray)
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            bitmap,
            "Title",
            null
        )
        return Uri.parse(path)
    }
}
