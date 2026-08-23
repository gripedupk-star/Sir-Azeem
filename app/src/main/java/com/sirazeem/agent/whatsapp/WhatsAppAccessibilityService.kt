package com.sirazeem.agent.whatsapp

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class WhatsAppAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally conservative.
        // WhatsApp UI automation is version-dependent. The first release uses
        // notification RemoteInput for replies and keeps UI automation as an extension point.
    }

    override fun onInterrupt() = Unit
}
