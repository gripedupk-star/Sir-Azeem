# Build / QA checklist

## Static checks
- [x] Android application namespace and ID are consistent.
- [x] Manifest declares notification listener service.
- [x] Manifest declares accessibility service.
- [x] Compose and Kotlin plugins are aligned.
- [x] GitHub Actions workflow builds `assembleDebug`.
- [x] No API key is embedded in source.
- [x] Knowledge is persisted locally.
- [x] Student IDs are generated sequentially.
- [x] Agent has explicit enabled/disabled and auto/approval controls.

## Runtime checks to perform on a real Android phone
- [ ] Grant notification access.
- [ ] Send a WhatsApp test message from a second account.
- [ ] Confirm notification is captured.
- [ ] Confirm a student record is created.
- [ ] Confirm message appears in local store.
- [ ] Confirm reply action is available on the device/WhatsApp build.
- [ ] Start in approval mode.
- [ ] Test owner-defined book instruction.
- [ ] Test unknown question escalation.
- [ ] Test app restart and verify knowledge persists.

## Production work still required before claiming full autonomous WhatsApp operation
- [ ] Official/authorized WhatsApp messaging integration where available, or device-specific accessibility automation tested against the exact WhatsApp version.
- [ ] Robust PDF/file sending workflow.
- [ ] Payment provider/API verification rather than screenshot-only verification.
- [ ] Encrypted secrets storage.
- [ ] Full Room database migration layer.
- [ ] Comprehensive automated tests.
- [ ] Release signing configuration.
- [ ] Crash reporting and telemetry with privacy controls.
