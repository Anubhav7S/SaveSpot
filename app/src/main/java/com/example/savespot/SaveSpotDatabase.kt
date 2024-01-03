package com.example.savespot

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [SaveSpotEntity::class], version =1, exportSchema = false)
abstract class SaveSpotDatabase:RoomDatabase() {
    abstract fun saveSpotDao():SaveSpotDao
    companion object{
        @Volatile
        private var INSTANCE:SaveSpotDatabase?=null
        fun getInstance(context:Context):SaveSpotDatabase{
            synchronized(this){
                var instance= INSTANCE
                if(instance==null){
                    instance=Room.databaseBuilder(context.applicationContext,SaveSpotDatabase::class.java,
                        "db_history").fallbackToDestructiveMigration().build()
                    INSTANCE=instance
                }
                return instance
            }
        }
    }
}