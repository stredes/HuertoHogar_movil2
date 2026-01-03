package com.example.huertohogar_mobil.network.mappers

import com.example.huertohogar_mobil.model.Solicitud
import com.example.huertohogar_mobil.network.dto.SolicitudAmistadDto

/**
 * Mappers para convertir entre SolicitudAmistadDto y Solicitud
 */

/**
 * Convierte SolicitudAmistadDto a Solicitud (Entity)
 */
fun SolicitudAmistadDto.toEntity(): Solicitud {
    return Solicitud(
        id = 0, // Room auto-genera el ID
        senderEmail = this.senderEmail,
        senderName = this.senderName,
        receiverEmail = this.receiverEmail,
        receiverName = this.receiverName ?: "",
        estado = this.estado,
        fechaCreacion = this.fechaCreacion,
        fechaActualizacion = this.fechaActualizacion
    )
}

/**
 * Convierte lista de SolicitudAmistadDto a lista de Solicitud
 */
fun List<SolicitudAmistadDto>.toEntityList(): List<Solicitud> {
    return this.map { it.toEntity() }
}

/**
 * Convierte Solicitud (Entity) a SolicitudAmistadDto
 */
fun Solicitud.toDto(): SolicitudAmistadDto {
    return SolicitudAmistadDto(
        id = this.id.toString(),
        senderEmail = this.senderEmail,
        senderName = this.senderName,
        receiverEmail = this.receiverEmail,
        receiverName = this.receiverName,
        estado = this.estado,
        fechaCreacion = this.fechaCreacion,
        fechaActualizacion = this.fechaActualizacion
    )
}

