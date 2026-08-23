package com.sirazeem.agent.whatsapp

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.sirazeem.agent.data.AgentStore
import com.sirazeem.agent.ai.AgentEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsAppNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var store: AgentStore
    private lateinit var engine: AgentEngine

    override fun onCreate() {
        super.onCreate()
        store = AgentStore(applicationContext)
        engine = AgentEngine(store)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.whatsapp" && sbn.packageName != "com.whatsapp.w4b") return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (text.isBlank()) return

        // WhatsApp notifications can represent groups and summaries.
        // We intentionally only use the notification title as a best-effort sender identifier.
        val student = store.findOrCreateStudent(phone = title.ifBlank { "unknown-${sbn.id}" }, name = title)
        store.addMessage(student.id, text, incoming = true)

        scope.launch {
            val settings = store.settings.value
            if (!settings.agentEnabled) return@launch

            val decision = engine.respond(text, student.id)
            if (decision.reply.isNullOrBlank()) return@launch

            // Automatic sending is attempted only when the notification exposes a RemoteInput reply action.
            // Otherwise the decision remains in the app for owner approval.
            if (settings.autoReply && !decision.requiresApproval) {
                replyToNotification(sbn, decision.reply)
            }
        }
    }

    private fun replyToNotification(sbn: StatusBarNotification, reply: String) {
        val actions = sbn.notification.actions ?: return
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            val input = remoteInputs.firstOrNull() ?: continue

            val bundle = Bundle()
            bundle.putCharSequence(input.resultKey, reply)

            val intent = Intent()
            RemoteInput.addResultsToIntent(arrayOf(input), intent, bundle)

            runCatching {
                action.actionIntent.send(this, 0, intent)
            }
            return
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
