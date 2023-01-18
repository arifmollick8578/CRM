package com.example.crm.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.crm.model.DraftListModel
import com.example.crm.model.ImageDetails
import com.example.crm.model.UserLocation
import com.example.crm.utility.FileTypeConverter

@Database(
    entities = [DraftListModel::class, ImageDetails::class, UserLocation::class],
    version = 4,                // <- Database version
    exportSchema = false,
    )
@TypeConverters(FileTypeConverter::class)
abstract class CrmDatabase : RoomDatabase() {

    abstract fun userDao(): DraftDao

    abstract fun imageDao(): ImageDao

    abstract fun locationDao(): UserLocationDao

    companion object {
        @Volatile
        private var INSTANCE: CrmDatabase? = null

        fun getDatabase(context: Context): CrmDatabase{
            val tempInstance = INSTANCE

            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrmDatabase::class.java,
                    "user_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}