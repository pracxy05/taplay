package com.taplay.wear.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.taplay.wear.data.WearSessionState
import java.nio.charset.StandardCharsets

/**
 * Taplay Wear HCE service.
 *
 * Supports dual-mode APDU:
 * 1. Proprietary Taplay AID (F0 01 02 03 04 05 06) — used by Taplay Reader Mode
 *    for ultra-fast direct token transmission ("CARD-WATCH-01").
 * 2. NFC Forum Type 4 Tag NDEF Application (D2 76 00 00 85 01 01) —
 *    used by Android OS default tag discovery. Emulates a Type 4 NDEF Tag
 *    containing "CARD-WATCH-01" + Android Application Record (AAR) for
 *    "com.taplay.app.emulator", preventing third-party apps like NFC Tools
 *    from reporting "not recognized" and automatically directing Android
 *    to open the Taplay Emulator!
 */
class TaplayWearApduService : HostApduService() {

    companion object {
        private const val TAG = "TaplayWearHCE"

        // The machine identifier this watch broadcasts over NFC
        const val MACHINE_ID = "CARD-WATCH-01"

        // Status Words (ISO 7816-4)
        private val SW_OK             = byteArrayOf(0x90.toByte(), 0x00)
        private val SW_FILE_NOT_FOUND = byteArrayOf(0x6A, 0x82.toByte())
        private val SW_UNKNOWN        = byteArrayOf(0x6F, 0x00)

        // Capability Container (CC) File (15 bytes)
        private val CC_FILE = byteArrayOf(
            0x00, 0x0F,           // CCLEN = 15 bytes
            0x20,                 // Mapping Version 2.0
            0x00, 0x3B,           // MLe (Max Read = 59 bytes)
            0x00, 0x34,           // MLc (Max Write = 52 bytes)
            0x04,                 // T = NDEF File Control TLV
            0x06,                 // L = 6 bytes
            0xE1.toByte(), 0x04,  // File ID = E104
            0x00, 0xFF.toByte(),  // Max NDEF size = 255 bytes
            0x00,                 // Read access = free
            0xFF.toByte()         // Write access = disabled
        )

        // Build NDEF File: 2-byte length prefix + NDEF Message (Text + AAR)
        private val NDEF_FILE: ByteArray by lazy {
            // Record 1: Text record "CARD-WATCH-01"
            val textPayload = byteArrayOf(0x02, 'e'.code.toByte(), 'n'.code.toByte()) +
                    MACHINE_ID.toByteArray(StandardCharsets.UTF_8)
            val textRecord = byteArrayOf(
                0xD1.toByte(), // MB=1, ME=0, SR=1, TNF=1
                0x01,          // Type length = 1
                textPayload.size.toByte(),
                'T'.code.toByte()
            ) + textPayload

            // Record 2: Android Application Record (AAR) for com.taplay.app.emulator
            val aarType = "android.com:pkg".toByteArray(StandardCharsets.UTF_8)
            val aarPayload = "com.taplay.app.emulator".toByteArray(StandardCharsets.UTF_8)
            val aarRecord = byteArrayOf(
                0x54,          // MB=0, ME=1, SR=1, TNF=4 (External)
                aarType.size.toByte(),
                aarPayload.size.toByte()
            ) + aarType + aarPayload

            val ndefMessage = textRecord + aarRecord
            val len = ndefMessage.size

            byteArrayOf((len shr 8).toByte(), (len and 0xFF).toByte()) + ndefMessage
        }
    }

    private var selectedFile: Int = 0 // 0: none, 1: CC (E103), 2: NDEF (E104)

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        if (commandApdu.isEmpty()) return SW_UNKNOWN
        val hex = commandApdu.toHex()
        Log.d(TAG, "processCommandApdu: $hex")

        val cla = commandApdu[0].toInt() and 0xFF
        val ins = if (commandApdu.size > 1) commandApdu[1].toInt() and 0xFF else 0

        // ── 1. SELECT COMMAND (00 A4 ...) ──────────────────────────────────
        if (ins == 0xA4) {
            val p1 = if (commandApdu.size > 2) commandApdu[2].toInt() and 0xFF else 0
            val p2 = if (commandApdu.size > 3) commandApdu[3].toInt() and 0xFF else 0

            // SELECT by AID (00 A4 04 00 ...)
            if (p1 == 0x04) {
                WearSessionState.onNfcTapSent()
                selectedFile = 0
                Log.d(TAG, "SELECT AID accepted -> SW_OK")
                return SW_OK
            }

            // SELECT by File ID (00 A4 00 0C ... or 00 A4 02 ...)
            if (commandApdu.size >= 7) {
                val fileId1 = commandApdu[5].toInt() and 0xFF
                val fileId2 = commandApdu[6].toInt() and 0xFF

                if (fileId1 == 0xE1 && fileId2 == 0x03) {
                    selectedFile = 1 // CC File
                    Log.d(TAG, "Selected CC File (E103)")
                    return SW_OK
                } else if (fileId1 == 0xE1 && fileId2 == 0x04) {
                    selectedFile = 2 // NDEF File
                    Log.d(TAG, "Selected NDEF File (E104)")
                    return SW_OK
                }
            }
            return SW_OK
        }

        // ── 2. READ BINARY (00 B0 ...) ─────────────────────────────────────
        if (ins == 0xB0) {
            val offset = if (commandApdu.size >= 4) {
                ((commandApdu[2].toInt() and 0xFF) shl 8) or (commandApdu[3].toInt() and 0xFF)
            } else 0
            val length = if (commandApdu.size >= 5) commandApdu[4].toInt() and 0xFF else 0

            WearSessionState.onNfcTapSent()

            when (selectedFile) {
                1 -> {
                    // Read CC File
                    val chunk = readChunk(CC_FILE, offset, length)
                    Log.d(TAG, "READ CC offset=$offset len=$length")
                    return chunk + SW_OK
                }
                2 -> {
                    // Read NDEF File
                    val chunk = readChunk(NDEF_FILE, offset, length)
                    Log.d(TAG, "READ NDEF offset=$offset len=$length")
                    return chunk + SW_OK
                }
                else -> {
                    // Direct Taplay proprietary READ — return CARD-WATCH-01 directly
                    Log.d(TAG, "Direct proprietary READ -> returning MACHINE_ID")
                    val idBytes = MACHINE_ID.toByteArray(StandardCharsets.UTF_8)
                    return idBytes + SW_OK
                }
            }
        }

        // Permissive fallback: If ANY reader queries data, send MACHINE_ID + 9000
        WearSessionState.onNfcTapSent()
        val idBytes = MACHINE_ID.toByteArray(StandardCharsets.UTF_8)
        return idBytes + SW_OK
    }

    private fun readChunk(src: ByteArray, offset: Int, length: Int): ByteArray {
        if (offset >= src.size) return byteArrayOf()
        val end = if (length == 0) src.size else minOf(offset + length, src.size)
        return src.copyOfRange(offset, end)
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE deactivated: reason=$reason")
        selectedFile = 0
    }

    private fun ByteArray.toHex(): String =
        joinToString(" ") { "%02X".format(it) }
}
