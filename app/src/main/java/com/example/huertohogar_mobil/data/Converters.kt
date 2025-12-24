package com.example.huertohogar_mobil.data

import androidx.room.TypeConverter
import com.example.huertohogar_mobil.model.EstadoPedido
import com.example.huertohogar_mobil.model.Producto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromProductoList(value: String?): List<Producto>? {
        if (value == null) return null
        val listType = object : TypeToken<List<Producto>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toProductoList(list: List<Producto>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromEstadoPedido(value: String?): EstadoPedido? {
        return value?.let { enumValueOf<EstadoPedido>(it) }
    }

    @TypeConverter
    fun toEstadoPedido(estado: EstadoPedido?): String? {
        return estado?.name
    }
}
