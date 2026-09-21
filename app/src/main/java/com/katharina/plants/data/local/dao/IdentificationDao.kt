package com.katharina.plants.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.katharina.plants.data.local.entity.IdentificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentification(entity: IdentificationEntity)

    @Query("SELECT * FROM identifications ORDER BY timestamp DESC")
    fun getAllIdentifications(): Flow<List<IdentificationEntity>>

    @Query("SELECT * FROM identifications WHERE id = :id")
    suspend fun getIdentificationById(id: Long): IdentificationEntity?

    @Query("DELETE FROM identifications WHERE id = :id")
    suspend fun deleteIdentification(id: Long)
}
