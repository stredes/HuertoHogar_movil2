package com.example.huertohogar_mobil.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.text.style.TextOverflow
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

// Datos reales y útiles para un blog de huertos
val blogPosts = listOf(
    BlogPost(
        1,
        "5 Pasos para tu Primer Huerto Urbano",
        "¿Tienes poco espacio? No importa. Aprende a cultivar tus propias hierbas y vegetales en balcones o ventanas con estos sencillos pasos.",
        """
        1. Elige el lugar adecuado: Busca una zona que reciba al menos 4-6 horas de luz solar directa al día. Las ventanas orientadas al norte suelen ser ideales.
        
        2. Selecciona tus macetas: Asegúrate de que tengan buen drenaje. Puedes reciclar envases de yogur o botellas plásticas si les haces agujeros en la base.
        
        3. Tierra de calidad: No uses tierra del jardín, ya que puede compactarse demasiado en macetas. Usa sustrato especial para macetas o una mezcla de turba y perlita.
        
        4. Qué cultivar: Para principiantes, recomendamos hierbas aromáticas (albahaca, menta, cilantro) o lechugas, ya que crecen rápido y ocupan poco espacio.
        
        5. Riego constante: En macetas, la tierra se seca más rápido. Revisa la humedad metiendo un dedo en la tierra; si está seco, riega. ¡Pero cuidado con encharcar!
        """.trimIndent(),
        "15 Oct 2024",
        Icons.Default.LocalFlorist
    ),
    BlogPost(
        2,
        "Calendario de Siembra: ¿Qué plantar ahora?",
        "Conoce los cultivos ideales para esta temporada. Aprovecha el clima actual para obtener las mejores cosechas de zanahorias y espinacas.",
        """
        Primavera - Verano:
        Es el momento ideal para cultivos de fruto como tomates, pimientos, berenjenas, calabacines y pepinos. También es buen momento para sembrar albahaca y girasoles. Recuerda aumentar el riego a medida que suben las temperaturas.
        
        Otoño - Invierno:
        No guardes tus herramientas todavía. Es la temporada perfecta para hortalizas de raíz y hoja.
        - Zanahorias: Siémbralas directamente en la tierra, sin trasplantar.
        - Espinacas y Acelgas: Resistentes al frío y muy productivas.
        - Ajos y Cebollas: Plántalos antes de las heladas fuertes para cosechar en primavera.
        """.trimIndent(),
        "22 Oct 2024",
        Icons.Default.CalendarToday
    ),
    BlogPost(
        3,
        "Cómo hacer Compost Casero sin Olores",
        "Transforma tus desechos de cocina en oro negro para tus plantas. Guía definitiva para compostar en departamentos sin molestar a los vecinos.",
        """
        El secreto de un compost sin olores es el equilibrio entre 'Verdes' (húmedos/nitrógeno) y 'Marrones' (secos/carbono).
        
        - Lo que SÍ debes echar (Verdes): Restos de frutas y verduras, posos de café, bolsitas de té, cáscaras de huevo trituradas.
        - Lo que SÍ debes echar (Marrones): Cartón sin tinta, papel de cocina, hojas secas, aserrín.
        - Lo que JAMÁS debes echar: Carne, huesos, lácteos, aceites o grasas (esto es lo que causa mal olor y atrae plagas).
        
        Regla de oro: Por cada puñado de residuos de cocina (verdes), agrega dos puñados de material seco (marrones) y mezcla bien para oxigenar. ¡En 2-3 meses tendrás abono gratis!
        """.trimIndent(),
        "01 Nov 2024",
        Icons.Default.Eco
    ),
    BlogPost(
        4,
        "Riego Eficiente: Ahorra agua y mejora tus plantas",
        "El exceso de riego es la causa #1 de muerte de plantas. Aprende técnicas sencillas para regar mejor gastando menos agua.",
        """
        Muchas veces regamos 'por si acaso', pero esto pudre las raíces. Aquí tienes trucos profesionales:
        
        1. Riego profundo pero espaciado: Es mejor regar mucho una vez a la semana que un poquito todos los días. Esto obliga a las raíces a crecer hacia abajo buscando humedad, haciendo plantas más fuertes.
        
        2. Mulching o Acolchado: Cubre la tierra alrededor de tus plantas con paja, hojas secas o corteza de pino. Esto evita que el sol evapore el agua del suelo, manteniendo la humedad por mucho más tiempo.
        
        3. Riega al amanecer o atardecer: Evita el mediodía. Con el sol fuerte, gran parte del agua se evapora antes de llegar a las raíces y las gotas sobre las hojas pueden hacer efecto lupa y quemarlas.
        """.trimIndent(),
        "10 Nov 2024",
        Icons.Default.WaterDrop
    ),
    BlogPost(
        5,
        "Control de Plagas Natural y Ecológico",
        "Olvídate de los químicos. Combate pulgones y orugas con remedios caseros seguros para tus mascotas y familia.",
        """
        - Para Pulgones: Mezcla una cucharada de jabón potásico (o jabón neutro biodegradable) en un litro de agua. Pulveriza sobre las hojas afectadas al atardecer.
        
        - Para Hongos (Oídio): Una mezcla de 1 parte de leche por 10 partes de agua funciona como un excelente fungicida natural gracias al ácido láctico.
        
        - Trampas Cromáticas: Los insectos se sienten atraídos por el color amarillo. Coloca tiras de plástico amarillo untadas con un poco de aceite cerca de tus plantas para atrapar moscas blancas y minadores sin usar veneno.
        """.trimIndent(),
        "18 Nov 2024",
        Icons.Default.WbSunny
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
                title = "Blog HuertoHogar",
                subtitle = "Consejos verdes para tu vida diaria",
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
                        color = MaterialTheme.colorScheme.outline
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
