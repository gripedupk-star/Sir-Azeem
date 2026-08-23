package com.sirazeem.agent.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sirazeem.agent.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AgentStore(context: Context) {
    private val prefs = context.getSharedPreferences("sir_azeem_store", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _knowledge = MutableStateFlow(loadList<KnowledgeItem>("knowledge"))
    val knowledge: StateFlow<List<KnowledgeItem>> = _knowledge.asStateFlow()

    private val _students = MutableStateFlow(loadList<Student>("students"))
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _messages = MutableStateFlow(loadList<ConversationMessage>("messages"))
    val messages: StateFlow<List<ConversationMessage>> = _messages.asStateFlow()

    private val _orders = MutableStateFlow(loadList<Order>("orders"))
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _files = MutableStateFlow(loadList<FileResource>("files"))
    val files: StateFlow<List<FileResource>> = _files.asStateFlow()

    private val _products = MutableStateFlow(loadList<Product>("products"))
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _settings = MutableStateFlow(
        load<AgentSettings>("settings") ?: AgentSettings()
    )
    val settings: StateFlow<AgentSettings> = _settings.asStateFlow()

    init {
        seedDefaults()
    }

    private inline fun <reified T> loadList(key: String): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<T>>(raw, object : TypeToken<List<T>>() {}.type)
        }.getOrDefault(emptyList())
    }

    private inline fun <reified T> load(key: String): T? {
        val raw = prefs.getString(key, null) ?: return null
        return runCatching { gson.fromJson<T>(raw, T::class.java) }.getOrNull()
    }

    private fun save(key: String, value: Any) {
        prefs.edit().putString(key, gson.toJson(value)).apply()
    }

    fun updateSettings(settings: AgentSettings) {
        _settings.value = settings
        save("settings", settings)
    }

    fun addKnowledge(title: String, instruction: String, category: String, priority: Int) {
        val item = KnowledgeItem(
            id = "K-" + UUID.randomUUID().toString().take(8).uppercase(),
            title = title.trim(),
            instruction = instruction.trim(),
            category = category.trim().ifBlank { "General" },
            priority = priority.coerceIn(0, 100)
        )
        val updated = listOf(item) + _knowledge.value
        _knowledge.value = updated
        save("knowledge", updated)
    }

    fun addFile(resource: FileResource) {
        val updated = listOf(resource) + _files.value
        _files.value = updated
        save("files", updated)
    }

    fun addProduct(product: Product) {
        val updated = listOf(product) + _products.value
        _products.value = updated
        save("products", updated)
    }

    fun findOrCreateStudent(phone: String, name: String = ""): Student {
        val normalized = phone.filter { it.isDigit() || it == '+' }
        val existing = _students.value.firstOrNull { it.phone == normalized }
        if (existing != null) return existing

        val nextNumber = (_students.value.mapNotNull {
            it.id.removePrefix("Student ").toIntOrNull()
        }.maxOrNull() ?: 0) + 1

        val student = Student(
            id = "Student %03d".format(nextNumber),
            name = name,
            phone = normalized
        )
        val updated = _students.value + student
        _students.value = updated
        save("students", updated)
        return student
    }

    fun addMessage(studentId: String, text: String, incoming: Boolean) {
        val message = ConversationMessage(
            id = UUID.randomUUID().toString(),
            studentId = studentId,
            text = text,
            incoming = incoming
        )
        val updatedMessages = _messages.value + message
        _messages.value = updatedMessages
        save("messages", updatedMessages)

        if (incoming) {
            val updatedStudents = _students.value.map {
                if (it.id == studentId) it.copy(
                    lastContact = System.currentTimeMillis(),
                    messageCount = it.messageCount + 1
                ) else it
            }
            _students.value = updatedStudents
            save("students", updatedStudents)
        }
    }

    fun addOrder(order: Order) {
        val updated = listOf(order) + _orders.value
        _orders.value = updated
        save("orders", updated)
    }

    fun stats(): AgentStats {
        val dayStart = java.time.LocalDate.now()
            .atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val todayMessages = _messages.value.count { it.timestamp >= dayStart }
        val todayReplies = _messages.value.count { !it.incoming && it.timestamp >= dayStart }
        val todayStudents = _students.value.count { it.firstContact >= dayStart }
        val todayOrders = _orders.value.count { it.createdAt >= dayStart }

        return AgentStats(
            messagesToday = todayMessages,
            repliesToday = todayReplies,
            newStudentsToday = todayStudents,
            numbersSavedToday = todayStudents,
            ordersToday = todayOrders,
            fileRequestsToday = 0,
            paymentsToday = 0,
            attentionCount = 0
        )
    }

    private fun seedDefaults() {
        if (_knowledge.value.isEmpty()) {
            addKnowledge(
                "English Book Order",
                "When a student asks how to order the English book, ask for Name, Contact number and Address. Price is Rs. 1500 and delivery is free.",
                "Orders",
                100
            )
        }
    }
}
