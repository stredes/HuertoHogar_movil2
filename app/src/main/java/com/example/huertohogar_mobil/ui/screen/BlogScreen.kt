package com.example.huertohogar_mobil.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.huertohogar_mobil.ui.components.HuertoCard
import com.example.huertohogar_mobil.ui.components.HuertoIconButton
import com.example.huertohogar_mobil.ui.components.SectionHeader

data class BlogPost(
    val id: Int,
    val title: String,
    val summary: String,
    val content: String,
    val date: String,
    val icon: ImageVector
)

// Datos actualizados: Reglas del Club / Red Privada
val blogPosts = listOf(
    BlogPost(
        1,
        "Acceso a la Distribución",
        "Reglas sobre quién puede gestionar y distribuir artículos en la red.",
        "Solo los Administradores (Admins) están autorizados para crear y vender productos. La activación de esta función tiene un costo asociado, garantizando la exclusividad y la seriedad de la red.",
        "Regla #1",
        Icons.Default.Lock
    ),
    BlogPost(
        2,
        "Verificación de Usuario",
        "Requisitos obligatorios para mantener el acceso y la seguridad del contacto.",
        "Los usuarios deben completar un registro riguroso para obtener acceso a la aplicación y poder contactar a su Proveedor designado. Es obligatorio agregar a la lista de contactos al individuo que facilitó el acceso a esta aplicación (\"el que te pasó la aplicación lo tienes que agregar\").",
        "Regla #2",
        Icons.Default.VerifiedUser
    ),
    BlogPost(
        3,
        "Protocolo de Discreción (Camuflaje)",
        "Uso obligatorio de códigos y terminología discreta.",
        "Los Administradores tienen prohibido publicar contenido de manera explícita. Deben recurrir al uso de camuflaje y códigos para referirse a los artículos. Ejemplos de camuflaje incluyen: \"aselga\", \"salcita\", \"el rosita\", entre otros.",
        "Regla #3",
        Icons.Default.VisibilityOff
    ),
    BlogPost(
        4,
        "Red Cerrada",
        "Política de admisión y consecuencias por acceso no autorizado.",
        "No se aceptarán usuarios que se encuentren fuera de la red común y conocida por los Administradores y el Root. La violación de esta regla resultará en consecuencias inmediatas, incluyendo el baneo, la eliminación de la cuenta y el bloqueo permanente del dispositivo.",
        "Regla #4",
        Icons.Default.Block
    ),
    BlogPost(
        5,
        "Sistema de Seguridad y Consecuencias",
        "Medidas contra infiltrados y monitoreo de seguridad.",
        "El sistema utiliza una base de datos de seguridad avanzada (referenciada como \"base de datos del gobierno\") para la localización de posibles infiltrados (\"zapos verdes y azules\") registrados en las fuerzas policiales gubernamentales. Aquellos identificados enfrentarán las consecuencias y de red correspondientes sepa que sabemos quiene es usted.",
        "Regla #5",
        Icons.Default.Security
    )
)

@Composable
fun BlogScreen() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Reglas de la Red",
                subtitle = "Normativa obligatoria para todos los miembros",
                centered = false
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(blogPosts) { post ->
            BlogPostCard(post)
        }
    }
}

@Composable
fun BlogPostCard(post: BlogPost) {
    var expanded by remember { mutableStateOf(false) }

    HuertoCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = post.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error // Rojo para resaltar que es una regla
                    )
                }
                HuertoIconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Colapsar" else "Expandir"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (expanded) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = post.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
