package com.bibliotecaplus.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authorName: String,
    val coverUrl: String?,
    val availableCopies: Int,
    val totalCopies: Int,
    val synopsis: String?,
    val year: Int?,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: String,
    val bookTitle: String,
    val bookCoverUrl: String?,
    val loanedAt: String,
    val dueDate: String,
    val returnedAt: String?,
    val status: String,
    val renewalCount: Int,
    val maxRenewals: Int,
    val cachedAt: Long = System.currentTimeMillis(),
)
