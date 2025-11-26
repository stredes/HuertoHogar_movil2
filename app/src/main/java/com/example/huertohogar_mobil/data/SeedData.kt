package com.example.huertohogar_mobil.data

import com.example.huertohogar_mobil.R
import com.example.huertohogar_mobil.model.Producto

object SeedData {
    val productos = listOf(
        Producto("1","Leche",1890,"1 L","Leche de vaca pasteurizada.", R.drawable.leche_1l),
        Producto("2","Manzana",1290,"kg","Manzana roja crocante.", R.drawable.manzana),
        Producto("3","Miel",4990,"frasco 500 g","Miel pura de abeja.", R.drawable.miel),
        Producto("4","Naranja",1190,"kg","Naranja jugosa.", R.drawable.naranja),
        Producto("5","Pimientos Tricolores",1890,"bandeja","Mix rojo, amarillo y naranja.", R.drawable.pimientos_tricolores),
        Producto("6","Plátanos Cavendish",990,"kg","Plátano maduro.", R.drawable.platanos_cavendish),
        Producto("7","Zanahoria",850,"kg","Zanahoria dulce y crocante.", R.drawable.zanahoria),
        Producto("8","Espinacas",1200,"bolsa","Hojas frescas.", R.drawable.espinacas)
    )
}
