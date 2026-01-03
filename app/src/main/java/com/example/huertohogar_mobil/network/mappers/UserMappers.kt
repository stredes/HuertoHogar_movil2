package com.example.huertohogar_mobil.network.mappers

import com.example.huertohogar_mobil.model.User
import com.example.huertohogar_mobil.network.dto.UsuarioDto
import com.example.huertohogar_mobil.network.dto.ProfileResponseDto

/**
 * Mappers para Usuario
 */

/**
 * Convierte UsuarioDto a User (entidad Room)
 */
fun UsuarioDto.toEntity(): User {
    return User(
        id = 0, // Room generará el ID
        name = this.name,
        email = this.email,
        passwordHash = "", // No guardamos contraseñas del backend
        role = this.role,
        rut = this.rut ?: ""
    )
}

/**
 * Convierte ProfileResponseDto a User
 */
fun ProfileResponseDto.toEntity(): User {
    return User(
        id = 0,
        name = this.name,
        email = this.email,
        passwordHash = "",
        role = this.role,
        rut = this.rut ?: ""
    )
}

/**
 * Convierte User a UsuarioDto básico
 */
fun User.toDto(): UsuarioDto {
    return UsuarioDto(
        id = this.id.toString(),
        name = this.name,
        email = this.email,
        rut = this.rut.ifEmpty { null },
        telefono = null,
        direccion = null,
        fotoPerfilUrl = null,
        role = this.role,
        emailVerificado = false,
        activo = true,
        fechaRegistro = System.currentTimeMillis(),
        ultimaActividad = null
    )
}

