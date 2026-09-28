package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.database.dao.ApiKeyDao
import com.example.data.local.database.dao.ConversationDao
import com.example.data.local.database.dao.MessageDao
import com.example.data.local.database.entity.ApiKeyEntity
import com.example.data.local.database.entity.ConversationEntity
import com.example.data.local.database.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ApiKeyEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wafa_ai_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate 10 default API Key slots
                            CoroutineScope(Dispatchers.IO).launch {
                                populateDefaultKeySlots(getDatabase(context).apiKeyDao())
                            }
                        }
                    })
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDefaultKeySlots(apiKeyDao: ApiKeyDao) {
            val defaultSlots = (1..10).map { index ->
                val label = String.format("API Key %02d", index)
                ApiKeyEntity(
                    slotIndex = index,
                    label = label,
                    encryptedKey = "",
                    maskedKey = "",
                    isSelected = false,
                    status = "NOT_CONFIGURED"
                )
            }
            apiKeyDao.insertDefaultSlots(defaultSlots)
        }
    }
}
