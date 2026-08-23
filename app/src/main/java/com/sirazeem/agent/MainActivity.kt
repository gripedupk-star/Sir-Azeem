package com.sirazeem.agent

import android.Manifest
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sirazeem.agent.data.AgentStore
import com.sirazeem.agent.model.AgentSettings
import com.sirazeem.agent.model.KnowledgeItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SirAzeemApp() }
    }
}

@Composable
private fun SirAzeemApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { AgentStore(context) }
    var selected by remember { mutableStateOf("Home") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
        if (Build.VERSION.SDK_INT >= 33) permissions += Manifest.permission.POST_NOTIFICATIONS
        permissionLauncher.launch(permissions.toTypedArray())
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF090C14),
            surface = Color(0xFF101522),
            primary = Color(0xFF8EA7FF)
        )
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .background(Color(0xFF090C14))
        ) {
            Sidebar(selected) { selected = it }
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selected) {
                    "Home" -> HomeScreen(store)
                    "Students" -> StudentsScreen(store)
                    "Orders" -> OrdersScreen(store)
                    "Files" -> FilesScreen(store)
                    "Knowledge" -> KnowledgeScreen(store)
                    "Teach Agent" -> TeachScreen(store)
                    "Settings" -> SettingsScreen(store, context)
                }
            }
        }
    }
}

@Composable
private fun Sidebar(selected: String, onSelect: (String) -> Unit) {
    val items = listOf(
        "Home" to Icons.Default.Home,
        "Students" to Icons.Default.People,
        "Orders" to Icons.Default.ShoppingBag,
        "Files" to Icons.Default.Description,
        "Knowledge" to Icons.Default.Psychology,
        "Teach Agent" to Icons.Default.School,
        "Settings" to Icons.Default.Settings
    )
    Column(
        Modifier
            .width(220.dp)
            .fillMaxHeight()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "SIR AZEEM",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp)
        )
        Text(
            "AI STUDENT AGENT",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = .55f),
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Spacer(Modifier.height(12.dp))
        items.forEachIndexed { index, (label, icon) ->
            val tint = listOf(
                Color(0xFF315DFF), Color(0xFF8C52FF), Color(0xFF00A8C6),
                Color(0xFFFF8A3D), Color(0xFF00A88A), Color(0xFFFFC44D),
                Color(0xFFEF476F)
            )[index]
            NavigationCard(label, icon, selected == label, tint) { onSelect(label) }
        }
    }
}

@Composable
private fun NavigationCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = tint.copy(alpha = if (selected) .30f else .12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .28f))
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = .92f))
            Spacer(Modifier.width(12.dp))
            Text(label, color = Color.White, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}

@Composable
private fun HomeScreen(store: AgentStore) {
    val stats = store.stats()
    LazyColumn(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Good morning, Sir Azeem", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Your personal AI student control center", color = Color.White.copy(alpha = .60f))
        }
        item {
            GlassCard("AI STATUS", "ACTIVE", Color(0xFF3B82F6))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("💬 Messages Today", stats.messagesToday.toString(), Color(0xFF00A8C6), Modifier.weight(1f))
                MetricCard("👨‍🎓 New Students", stats.newStudentsToday.toString(), Color(0xFF8C52FF), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("📱 Numbers Saved", stats.numbersSavedToday.toString(), Color(0xFF00A88A), Modifier.weight(1f))
                MetricCard("📦 Orders Today", stats.ordersToday.toString(), Color(0xFFFF8A3D), Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("📚 File Requests", stats.fileRequestsToday.toString(), Color(0xFFEF476F), Modifier.weight(1f))
                MetricCard("⚠️ Attention", stats.attentionCount.toString(), Color(0xFFE53935), Modifier.weight(1f))
            }
        }
        item {
            GlassCard(
                "TEACH AGENT",
                "Add permanent knowledge from natural-language instructions.",
                Color(0xFFFFC44D)
            )
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, tint: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = tint.copy(alpha = .16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .30f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun GlassCard(title: String, value: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = tint.copy(alpha = .16f),
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = .32f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp)) {
            Text(title, color = Color.White.copy(alpha = .72f), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StudentsScreen(store: AgentStore) {
    val students by store.students.collectAsState()
    ScreenTitle("Students", "Automatically detected student records")
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(students) { s ->
            GlassCard(
                "${s.id}  •  ${s.name.ifBlank { "Unknown name" }}",
                "${s.phone}  •  ${s.messageCount} messages",
                Color(0xFF8C52FF)
            )
        }
    }
}

@Composable
private fun OrdersScreen(store: AgentStore) {
    val orders by store.orders.collectAsState()
    ScreenTitle("Orders", "Book and product orders")
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(orders) { o ->
            GlassCard(
                o.id,
                "${o.customerName} • ${o.phone} • Rs. ${o.amount} • ${o.status}",
                Color(0xFFFF8A3D)
            )
        }
    }
}

@Composable
private fun FilesScreen(store: AgentStore) {
    val files by store.files.collectAsState()
    ScreenTitle("Files", "Free and paid educational resources")
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(files) { f ->
            GlassCard(
                f.name,
                if (f.paid) "PAID • Rs. ${f.price} • ${f.paymentMethod}" else "FREE • AUTO SEND",
                Color(0xFFEF476F)
            )
        }
    }
}

@Composable
private fun KnowledgeScreen(store: AgentStore) {
    val knowledge by store.knowledge.collectAsState()
    ScreenTitle("Knowledge", "Permanent owner-approved instructions")
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(knowledge) { k ->
            GlassCard(
                "${k.title}  •  ${k.category}",
                k.instruction,
                Color(0xFF00A88A)
            )
        }
    }
}

@Composable
private fun TeachScreen(store: AgentStore) {
    var title by remember { mutableStateOf("") }
    var instruction by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var priority by remember { mutableStateOf("80") }
    var saved by remember { mutableStateOf(false) }

    ScreenTitle("Teach Agent", "Write an instruction once; store it permanently.")
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(title, { title = it }, label = { Text("Knowledge title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            instruction, { instruction = it },
            label = { Text("What should the agent remember?") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp)
        )
        OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(priority, { priority = it.filter(Char::isDigit) }, label = { Text("Priority 0–100") }, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = {
                if (title.isNotBlank() && instruction.isNotBlank()) {
                    store.addKnowledge(title, instruction, category, priority.toIntOrNull() ?: 80)
                    title = ""
                    instruction = ""
                    saved = true
                }
            }
        ) { Text("SAVE & TEACH") }
        AnimatedVisibility(saved) {
            Text("✓ Permanently saved", color = Color(0xFF7CFFB2), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingsScreen(store: AgentStore, context: Context) {
    val current by store.settings.collectAsState()
    var endpoint by remember(current.aiEndpoint) { mutableStateOf(current.aiEndpoint) }
    var key by remember(current.aiApiKey) { mutableStateOf(current.aiApiKey) }
    var model by remember(current.aiModel) { mutableStateOf(current.aiModel) }

    ScreenTitle("Settings", "Agent controls and Android permissions")
    LazyColumn(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            GlassCard(
                "WHATSAPP MONITOR",
                if (isNotificationListenerEnabled(context)) "Enabled" else "Open Android notification access to enable it.",
                Color(0xFF3B82F6)
            )
        }
        item {
            Button(onClick = {
                context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }) { Text("OPEN NOTIFICATION ACCESS") }
        }
        item {
            GlassCard(
                "ACCESSIBILITY",
                "Optional extension point for WhatsApp UI actions. Android/WhatsApp UI changes can affect automation.",
                Color(0xFFFFC44D)
            )
        }
        item {
            Button(onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }) { Text("OPEN ACCESSIBILITY SETTINGS") }
        }
        item {
            HorizontalDivider()
            Text("AI ENDPOINT", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(endpoint, { endpoint = it }, label = { Text("OpenAI-compatible base URL") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(key, { key = it }, label = { Text("API key") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(model, { model = it }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Agent enabled", Modifier.weight(1f))
                Switch(
                    checked = current.agentEnabled,
                    onCheckedChange = {
                        store.updateSettings(current.copy(agentEnabled = it))
                    }
                )
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto reply", Modifier.weight(1f))
                Switch(
                    checked = current.autoReply,
                    onCheckedChange = {
                        store.updateSettings(current.copy(autoReply = it, approvalMode = !it))
                    }
                )
            }
        }
        item {
            Button(onClick = {
                store.updateSettings(
                    current.copy(
                        aiEndpoint = endpoint.trim(),
                        aiApiKey = key.trim(),
                        aiModel = model.trim()
                    )
                )
            }) { Text("SAVE AI SETTINGS") }
        }
    }
}

@Composable
private fun ScreenTitle(title: String, subtitle: String) {
    Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.White.copy(alpha = .60f))
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val component = ComponentName(context, com.sirazeem.agent.whatsapp.WhatsAppNotificationListener::class.java)
    return manager.isNotificationListenerAccessGranted(component)
}
