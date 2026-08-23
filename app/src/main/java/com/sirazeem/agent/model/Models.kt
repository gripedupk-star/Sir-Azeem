package com.sirazeem.agent.model

data class KnowledgeItem(
    val id: String,
    val title: String,
    val instruction: String,
    val category: String = "General",
    val priority: Int = 50,
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class Student(
    val id: String,
    val name: String = "",
    val phone: String,
    val firstContact: Long = System.currentTimeMillis(),
    val lastContact: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val savedAsContact: Boolean = false
)

data class ConversationMessage(
    val id: String,
    val studentId: String,
    val text: String,
    val incoming: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class Product(
    val id: String,
    val name: String,
    val price: Int,
    val deliveryFee: Int = 0,
    val requiredFields: List<String> = emptyList(),
    val active: Boolean = true
)

data class FileResource(
    val id: String,
    val name: String,
    val uri: String,
    val description: String = "",
    val paid: Boolean = false,
    val price: Int = 0,
    val paymentMethod: String = "",
    val paymentNumber: String = "",
    val active: Boolean = true
)

data class Order(
    val id: String,
    val studentId: String,
    val productId: String,
    val customerName: String = "",
    val phone: String = "",
    val address: String = "",
    val amount: Int = 0,
    val status: String = "NEW",
    val createdAt: Long = System.currentTimeMillis()
)

data class AgentSettings(
    val agentEnabled: Boolean = true,
    val autoReply: Boolean = false,
    val approvalMode: Boolean = true,
    val ownerName: String = "Sir Azeem",
    val aiEndpoint: String = "",
    val aiApiKey: String = "",
    val aiModel: String = ""
)

data class AgentStats(
    val messagesToday: Int = 0,
    val repliesToday: Int = 0,
    val newStudentsToday: Int = 0,
    val numbersSavedToday: Int = 0,
    val ordersToday: Int = 0,
    val fileRequestsToday: Int = 0,
    val paymentsToday: Int = 0,
    val attentionCount: Int = 0
)
