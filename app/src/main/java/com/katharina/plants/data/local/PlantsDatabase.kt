package com.katharina.plants.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.katharina.plants.data.local.dao.IdentificationDao
import com.katharina.plants.data.local.entity.IdentificationEntity

@Database(
    entities = [IdentificationEntity::class],
    version = 1
)
abstract class PlantsDatabase : RoomDatabase() {
    abstract val dao: IdentificationDao
}
