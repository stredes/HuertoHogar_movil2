package com.example.huertohogar_mobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.huertohogar_mobil.data.Producto
import com.example.huertohogar_mobil.navigation.Routes
import com.example.huertohogar_mobil.ui.screens.*
import com.example.huertohogar_mobil.ui.theme.HuertoHogarMobilTheme
import com.example.huertohogar_mobil.viewmodel.MarketViewModel
import com.example.huertohogar_mobil.viewmodel.MarketViewModelFactory
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModel SIN Hilt (Factory)
        val vm = ViewModelProvider(this, MarketViewModelFactory())
            .get(MarketViewModel::class.java)

        setContent {
            HuertoHogarMobilTheme {
                val nav = rememberNavController()
                val ui = vm.ui.collectAsStateWithLifecycle().value

                Scaffold(
                    bottomBar = { BottomNavBar(navController = nav) }
                ) { paddingValues ->
                    NavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = nav,
                        startDestination = Routes.Inicio.route
                    ) {
                        composable(Routes.Inicio.route) {
                            InicioScreen(onNavigateToProductos = { nav.navigate(Routes.Productos.route) })
                        }
                        composable(Routes.Productos.route) {
                            TopScaffold(
                                title = "Productos 🥑",
                                countCarrito = ui.countCarrito,
                                onCarrito = { nav.navigate(Routes.Carrito.route) }
                            ) { pv ->
                                CatalogoMinimal(
                                    productos = ui.productosFiltrados,
                                    onBuscar = vm::setQuery,
                                    onVer = { p ->
                                        vm.seleccionar(p)
                                        nav.navigate(Routes.Detalle.create(p.id))
                                    },
                                    onAgregar = { p -> vm.agregar(p, +1) },
                                    paddingValues = pv
                                )
                            }
                        }
                        composable(Routes.Nosotros.route) {
                            NosotrosScreen()
                        }
                        composable(Routes.Blog.route) {
                            BlogScreen()
                        }
                        composable(Routes.Contacto.route) {
                            ContactoScreen()
                        }
                        composable(Routes.IniciarSesion.route) {
                            IniciarSesionScreen()
                        }
                        composable(Routes.Registrarse.route) {
                            RegistrarseScreen()
                        }
                        composable(Routes.FinPago.route) {
                            FinPagoScreen(onVolverAlInicio = {
                                nav.navigate(Routes.Inicio.route) {
                                    popUpTo(Routes.Inicio.route) { inclusive = true }
                                }
                            })
                        }

                        // Detalle
                        composable(
                            route = Routes.Detalle.route,
                            arguments = listOf(
                                navArgument(Routes.Detalle.ARG_ID) { type = NavType.StringType }
                            )
                        ) { backStack ->
                            val id = backStack.arguments?.getString(Routes.Detalle.ARG_ID)
                            val producto = ui.productos.firstOrNull { it.id == id }
                            if (producto == null) {
                                nav.popBackStack()
                            } else {
                                TopScaffold(
                                    title = producto.nombre,
                                    countCarrito = ui.countCarrito,
                                    onBack = { nav.popBackStack() },
                                    onCarrito = { nav.navigate(Routes.Carrito.route) }
                                ) { pv ->
                                    DetalleMinimal(
                                        p = producto,
                                        onAgregar = { vm.agregar(producto, +1) },
                                        paddingValues = pv
                                    )
                                }
                            }
                        }

                        // Carrito
                        composable(Routes.Carrito.route) {
                            TopScaffold(
                                title = "Carrito",
                                onBack = { nav.popBackStack() }
                            ) { pv ->
                                CarritoMinimal(
                                    lineas = ui.carrito.mapNotNull { (id, qty) ->
                                        ui.productos.firstOrNull { it.id == id }?.let { it to qty }
                                    },
                                    onSumar = { p -> vm.agregar(p, +1) },
                                    onRestar = { p -> vm.agregar(p, -1) },
                                    total = ui.totalCLP,
                                    onCheckout = { nav.navigate(Routes.Checkout.route) },
                                    paddingValues = pv
                                )
                            }
                        }

                        // Checkout
                        composable(Routes.Checkout.route) {
                            TopScaffold(
                                title = "Checkout",
                                onBack = { nav.popBackStack() }
                            ) { pv ->
                                CheckoutMinimal(
                                    lineas = ui.carrito.mapNotNull { (id, qty) ->
                                        ui.productos.firstOrNull { it.id == id }?.let { it to qty }
                                    },
                                    total = ui.totalCLP,
                                    onFinalizar = {
                                        vm.limpiarCarrito()
                                        nav.navigate(Routes.FinPago.route)
                                    },
                                    paddingValues = pv
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Routes.Inicio to Icons.Default.Home,
        Routes.Productos to Icons.Default.Store,
        Routes.Nosotros to Icons.Default.Info,
        Routes.Blog to Icons.Default.Article,
        Routes.Contacto to Icons.Default.Email,
        Routes.IniciarSesion to Icons.Default.Person,
        Routes.Registrarse to Icons.Default.PersonAdd,
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { (screen, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}


/* -------------------- UI Helpers mínimas (provisorias) -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopScaffold(
    title: String,
    countCarrito: Int = 0,
    onBack: (() -> Unit)? = null,
    onCarrito: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val navIcon: @Composable () -> Unit = if (onBack != null) {
        {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
    } else { { } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = navIcon,
                actions = {
                    if (onCarrito != null) {
                        BadgedBox(badge = {
                            if (countCarrito > 0) Badge { Text("$countCarrito") }
                        }) {
                            IconButton(onClick = onCarrito) {
                                Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = content
    )
}

@Composable
private fun CatalogoMinimal(
    productos: List<Producto>,
    onBuscar: (String) -> Unit,
    onVer: (Producto) -> Unit,
    onAgregar: (Producto) -> Unit,
    paddingValues: PaddingValues
) {
    Column(Modifier.padding(paddingValues)) {
        var query by remember { mutableStateOf("") }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; onBuscar(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            label = { Text("Buscar producto...") },
            singleLine = true
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(productos, key = { it.id }) { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVer(p) }
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shape = RoundedCornerShape(12.dp)
                        if (p.imagenResId != null) {
                            Image(
                                painter = painterResource(id = p.imagenResId),
                                contentDescription = p.nombre,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(shape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.nombre, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Text(p.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${clp(p.precioCLP)} / ${p.unidad}")
                        }
                        IconButton(onClick = { onAgregar(p) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Agregar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetalleMinimal(
    p: Producto,
    onAgregar: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val shape = RoundedCornerShape(16.dp)
        if (p.imagenResId != null) {
            Image(
                painter = painterResource(id = p.imagenResId),
                contentDescription = p.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(shape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(p.descripcion)
        Spacer(Modifier.height(8.dp))
        Text("${clp(p.precioCLP)} / ${p.unidad}", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAgregar) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Agregar")
        }
    }
}

@Composable
private fun CarritoMinimal(
    lineas: List<Pair<Producto, Int>>,
    onSumar: (Producto) -> Unit,
    onRestar: (Producto) -> Unit,
    total: Int,
    onCheckout: () -> Unit,
    paddingValues: PaddingValues
) {
    Scaffold(
        bottomBar = {
            if (lineas.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total: ${clp(total)}", fontWeight = FontWeight.Bold)
                        Button(onClick = onCheckout) {
                            Icon(Icons.Filled.Check, contentDescription = null)
                            Spacer(Modifier.width(6.dp)); Text("Continuar")
                        }
                    }
                }
            }
        }
    ) { inner ->
        if (lineas.isEmpty()) {
            Box(
                Modifier
                    .padding(paddingValues)
                    .padding(inner)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Tu carrito está vacío") }
        } else {
            LazyColumn(
                Modifier
                    .padding(paddingValues)
                    .padding(inner)
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lineas, key = { it.first.id }) { (p, qty) ->
                    Card {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.nombre, fontWeight = FontWeight.SemiBold)
                                Text("${clp(p.precioCLP)} / ${p.unidad}")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onRestar(p) }) { Text("−", fontSize = 20.sp) }
                                Text("$qty", modifier = Modifier.width(28.dp))
                                IconButton(onClick = { onSumar(p) }) {
                                    Icon(Icons.Filled.Add, contentDescription = "Más")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckoutMinimal(
    lineas: List<Pair<Producto, Int>>,
    total: Int,
    onFinalizar: () -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Resumen de compra", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        lineas.forEach { (p, qty) ->
            Text("- $qty x ${p.nombre} (${clp(p.precioCLP)})")
        }
        HorizontalDivider()
        Text("Total: ${clp(total)}", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onFinalizar) { Text("Finalizar compra") }
    }
}

/* util CLP */
@Suppress("DEPRECATION")
private fun clp(v: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(v)
