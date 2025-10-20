package com.example.huertohogar_mobil.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Post(val title: String, val content: String)

val blogPosts = listOf(
    Post("Cómo empezar tu propio huerto en casa", "Lorem ipsum dolor sit amet, consectetur adipiscing elit..."),
    Post("Los beneficios de comer productos orgánicos", "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua..."),
    Post("Recetas frescas para el verano", "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat...")
)

@Composable
fun BlogScreen() {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Nuestro Blog",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(blogPosts) { post ->
            BlogPostCard(post)
        }
    }
}

@Composable
fun BlogPostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content)
        }
    }
}
