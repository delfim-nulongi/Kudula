package com.example.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class KudulaTypeConverters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value.isNullOrEmpty()) return "[]"
        val jsonArray = JSONArray()
        value.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val jsonArray = JSONArray(value)
            val list = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
            list
        } catch (_: Exception) {
            // Suporte para delimitador legado "||"
            value.split("||").filter { it.isNotBlank() }
        }
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString(",")
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",").mapNotNull { it.toFloatOrNull() }
    }
}
