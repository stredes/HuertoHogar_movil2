package com.example.huertohogar_mobil.network.mappers

import com.example.huertohogar_mobil.model.MensajeChat
import com.example.huertohogar_mobil.network.dto.MensajeChatDto

/**
 * Mappers para convertir entre MensajeChatDto y MensajeChat
 */

/**
 * Convierte MensajeChatDto a MensajeChat (Entity)
 */
fun MensajeChatDto.toEntity(): MensajeChat {
    return MensajeChat(
        id = this.id ?: 0,
        remitenteEmail = this.remitenteEmail,
        destinatarioEmail = this.destinatarioEmail,
        contenido = this.contenido,
        tipoContenido = this.tipoContenido,
        timestamp = this.timestamp,
        leido = this.leido
    )
}

/**
 * Convierte lista de MensajeChatDto a lista de MensajeChat
 */
fun List<MensajeChatDto>.toEntityList(): List<MensajeChat> {
    return this.map { it.toEntity() }
}

/**
 * Convierte MensajeChat (Entity) a MensajeChatDto
 */
fun MensajeChat.toDto(): MensajeChatDto {
    return MensajeChatDto(
        id = this.id.toString(),
        remitenteEmail = this.remitenteEmail ?: "",
        destinatarioEmail = this.destinatarioEmail ?: "",
        contenido = this.contenido,
        tipoContenido = this.tipoContenido,
        timestamp = this.timestamp,
        leido = this.leido
    )
}

