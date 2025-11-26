package com.example.huertohogar_mobil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.huertohogar_mobil.data.MensajeRepository
import com.example.huertohogar_mobil.model.MensajeContacto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val mensajeRepo: MensajeRepository
) : ViewModel() {

    val mensajes: StateFlow<List<MensajeContacto>> = mensajeRepo.getMensajes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun marcarComoRespondido(id: Int, actual: Boolean) {
        viewModelScope.launch {
            mensajeRepo.toggleRespondido(id, actual)
        }
    }

    fun eliminarMensaje(mensaje: MensajeContacto) {
        viewModelScope.launch {
            mensajeRepo.eliminarMensaje(mensaje)
        }
    }
}
