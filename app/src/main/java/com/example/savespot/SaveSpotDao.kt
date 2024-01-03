package com.example.savespot

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface SaveSpotDao {
    @Insert
    suspend fun insert(saveSpotEntity:SaveSpotEntity)

    @Delete
    suspend fun delete(saveSpotEntity:SaveSpotEntity)

    @Update
    suspend fun update(saveSpotEntity:SaveSpotEntity)

    @Query("SELECT * FROM `spot-table`")
    fun fetchAllPlace(): Flow<List<SaveSpotEntity>>

    @Query("SELECT * FROM `spot-table` WHERE id=:id")
    fun fetchPlaceById(id: Int): Flow<List<SaveSpotEntity>>
}



