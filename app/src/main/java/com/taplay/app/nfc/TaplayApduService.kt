package com.taplay.app.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.nio.charset.StandardCharsets

class TaplayApduService : HostApduService() {

    companion object {
        private const val TAG = "TaplayApduService"
        
        // Dynamic Machine Identifier read by Phone 1 over NFC
        const val MACHINE_ID = "MACH-PHONE-2"
        
        // Command APDUs
        private val SELECT_AID_COMMAND = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(),
            0xF0.toByte(), 0x01.toByte(), 0x02.toByte(), 0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )
        
        private val READ_BINARY_COMMAND = byteArrayOf(
            0x00.toByte(), 0xB0.toByte(), 0x00.toByte(), 0x00.toByte()
        )

        // Status APDU responses
        private val STATUS_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val STATUS_FAILED = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return STATUS_FAILED

        Log.d(TAG, "Received APDU command: ${commandApdu.toHexString()}")

        // Check if command is SELECT AID
        if (isSelectAidCommand(commandApdu)) {
            Log.d(TAG, "SELECT AID command recognized. Emulating arcade machine card.")
            return STATUS_SUCCESS
        }

        // Emit the dynamic machine identifier string that Phone 1 reads as the tagId.
        // The backend uses this value as machineId to register the active session,
        // which Phone 2 (this device) polls via GET /api/v1/transaction/status?machineId=MACH-PHONE-2
        val machineId = MACHINE_ID
        val payloadBytes = machineId.toByteArray(StandardCharsets.UTF_8)
        Log.d(TAG, "Sending HCE machine identifier: $machineId (${payloadBytes.size} bytes)")

        // Append 0x9000 status bytes to response
        return payloadBytes + STATUS_SUCCESS
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE Service Deactivated. Reason: $reason")
    }

    private fun isSelectAidCommand(apdu: ByteArray): Boolean {
        if (apdu.size < SELECT_AID_COMMAND.size) return false
        for (i in SELECT_AID_COMMAND.indices) {
            if (apdu[i] != SELECT_AID_COMMAND[i]) return false
        }
        return true
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }
}