package com.bibliotecaplus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bibliotecaplus.data.local.entities.BookEntity
import com.bibliotecaplus.data.local.entities.LoanEntity
import com.bibliotecaplus.data.local.dao.BookDao
import com.bibliotecaplus.data.local.dao.LoanDao

@Database(
    entities = [BookEntity::class, LoanEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun loanDao(): LoanDao
}
