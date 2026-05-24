package com.bibliotecaplus.data.local.dao

import androidx.room.*
import com.bibliotecaplus.data.local.entities.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY loanedAt DESC")
    fun getAll(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status IN ('ACTIVE', 'OVERDUE') ORDER BY dueDate ASC")
    fun getActive(): Flow<List<LoanEntity>>

    @Upsert
    suspend fun upsertAll(loans: List<LoanEntity>)

    @Query("DELETE FROM loans")
    suspend fun deleteAll()
}
