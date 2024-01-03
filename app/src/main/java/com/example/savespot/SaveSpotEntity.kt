package com.example.savespot

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spot-table")
data class SaveSpotEntity(@PrimaryKey(autoGenerate = true) val id:Int, val title:String, val image:String, val description:String,
val date:String, val location:String, val latitude:Double, val longitude:Double):java.io.Serializable