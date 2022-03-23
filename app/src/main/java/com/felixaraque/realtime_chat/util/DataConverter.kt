package com.felixaraque.realtime_chat.util

import androidx.room.TypeConverter
import java.util.*

object DataConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}