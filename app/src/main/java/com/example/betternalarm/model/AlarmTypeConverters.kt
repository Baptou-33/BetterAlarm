package com.example.betternalarm.model

import androidx.room.TypeConverter

class AlarmTypeConverters {
    @TypeConverter
    fun fromRepetitionType(type: RepetitionType): String {
        return type.name
    }
    
    @TypeConverter
    fun toRepetitionType(value: String): RepetitionType {
        return RepetitionType.valueOf(value)
    }
    
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return list.joinToString(",")
    }
    
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }
    }
    
    @TypeConverter
    fun fromLongList(list: List<Long>): String {
        return list.joinToString(",")
    }
    
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toLong() }
    }
}
