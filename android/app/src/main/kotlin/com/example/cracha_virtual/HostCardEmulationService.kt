package com.example.cracha_virtual // Use seu pacote correto

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HostCardEmulationService : HostApduService() {

    companion object {
        const val TAG = "HostCardEmulationService"
        // Este é um comando "SELECT AID" padrão. A parte final é o seu AID.
        // CLA INS P1 P2 Lc  <-- Cabeçalho do Comando
        // 00  A4  04 00 07  <-- Valores comuns
        // F0A1B2C3D4E5F6      <-- Seu AID (Data)
        val SELECT_APDU = byteArrayOf(
            0x00.toByte(), // CLA
            0xA4.toByte(), // INS
            0x04.toByte(), // P1
            0x00.toByte(), // P2
            0x07.toByte(), // Lc (comprimento do AID)
            0xF0.toByte(), 0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte(), 0xE5.toByte(), 0xF6.toByte() // AID
        )
        // Definição dos códigos de status
        val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val SW_UNKNOWN_COMMAND = byteArrayOf(0x6D.toByte(), 0x00.toByte()) // Instruction code not supported or invalid
        val SW_UNKNOWN_COMMAND2 = byteArrayOf(0xA1.toByte(), 0x00.toByte()) // Instruction code not supported or invalid
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        if (commandApdu == null) {
            return SW_UNKNOWN_COMMAND2
        }

        Log.d(TAG, "Comando APDU recebido: ${commandApdu.toHexString()}")

        // Verifica se o comando recebido é o "SELECT AID" que esperamos
        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.i(TAG, "Comando SELECT AID correto. Respondendo com sucesso.")
            val responseMessage = "ACESSO_LIBERADO"
            // Ao enviar nossa própria resposta + SW_OK, temos controle total
            return responseMessage.toByteArray(Charsets.UTF_8) + SW_OK
        } else {
            Log.w(TAG, "Comando desconhecido recebido.")
            // Se o comando não for o que esperamos, enviamos um código de erro.
            return SW_UNKNOWN_COMMAND
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Desativado: Razão -> $reason")
    }
}

// Uma função útil para ver os bytes como texto no Logcat
fun ByteArray.toHexString(): String = joinToString(separator = " ") { "%02X".format(it) }