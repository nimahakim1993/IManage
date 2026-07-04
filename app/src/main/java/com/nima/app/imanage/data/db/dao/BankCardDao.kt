package com.nima.app.imanage.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nima.app.imanage.data.db.entity.BankCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BankCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: BankCardEntity)

    @Delete
    suspend fun delete(card: BankCardEntity)

    @Query("SELECT * FROM bank_cards ORDER BY sortKey ASC")
    fun getAll(): Flow<List<BankCardEntity>>

    @Query("SELECT * FROM bank_cards WHERE id = :cardId")
    fun get(cardId: Int): Flow<BankCardEntity?>

    @Query("UPDATE bank_cards SET sortKey = :sortKey WHERE id = :id")
    suspend fun updateSortKey(id: Int, sortKey: Int)
}