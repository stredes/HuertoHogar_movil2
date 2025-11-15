package com.example.huertohogar_mobil.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "pedidos",
    foreignKeys = [ForeignKey(
        entity = Cliente::class,
        parentColumns = ["id"],
        childColumns = ["clienteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Pedido(
    @PrimaryKey
    val id: String,
    val clienteId: String,
    val fecha: Date,
    val total: Int
)
