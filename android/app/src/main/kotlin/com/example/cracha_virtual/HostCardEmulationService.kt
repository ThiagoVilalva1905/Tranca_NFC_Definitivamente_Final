/**
Esse código funiona da seguinte maneira:

Critérios: 
a entrada tem que ter no máximo 27 caracteres.
Só é permitido os seguinte caracteres (que no código foi definido como ALFABETO): abcdefghijklmnopqrstuvwxyz._-
a saida pode ter no máximo 20 bytes (incluindo os bytes de validação)

Exemplo usando -> m.p
A entrada tem 3 caracteres (está dentro do limite de 27 caracteres);
Os caracteres estão dentro dos permitidos;

cada caractere é mapeado para sua posição (índice) no nosso ALFABETO e, em seguida, convertido para uma representação binária de bits.
m -> 12 -> 01100
. -> 26 -> 11010
p -> 15 -> 01111

osa bits são juntado para obter uma única sequência:
011001101001111

Agora, essa sequência  é "empacotada" em bytes (grupos de 8 bits):
primeiro byte:
01100110 -> convertendo para hex fica 66
segundo byte:
Pega os 7 bits restantes e completa com um 0 no final para formar um byte completo:
10011110 -> convertido para hex fica 9E

Assim, m.p foi comprimido para apenas 2 bytes -> 669E

por fim, vem a montagem do payload final:
Primeiro byte: tamanho + resultado da compressão + 90 00 (validação)

no exemlo fica: 03 66 9E 90 00
*/

// Define o pacote ao qual este arquivo pertence.
package com.example.cracha_virtual // Use seu pacote correto

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Serviço de emulação de cartão (HCE) que responde a comandos de leitores NFC.
 * Esta versão utiliza uma codificação customizada de 5 bits para enviar dados.
 */
class HostCardEmulationService : HostApduService() {

    companion object {
        const val TAG = "HostCardEmulationService"

        // Comando APDU "SELECT AID" esperado do leitor NFC.
        val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x07.toByte(),
            0xF0.toByte(), 0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte(), 0xE5.toByte(), 0xF6.toByte()
        )

        // Status Words (SW) - Respostas de status de 2 bytes.
        val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        val SW_UNKNOWN_COMMAND = byteArrayOf(0x6D.toByte(), 0x00.toByte())
    }

   
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return SW_UNKNOWN_COMMAND
        }

        Log.d(TAG, "Comando APDU recebido: ${commandApdu.toHexString()}")

        // Verifica se o comando recebido é o SELECT AID que esperamos.
        if (commandApdu.contentEquals(SELECT_APDU)) {
            Log.i(TAG, "Comando SELECT AID correto. Codificando e enviando a resposta.")


            // INÍCIO DA LÓGICA PARA CODIFICAR

            // 1. Defina aqui o identificador que será enviado.
            // A entrada deve ter no máximo 27 caracteres e usar apenas o alfabeto permitido.
            val userIdentifier = "m.p" // <-- CÓDIGO AJUSTADO AQUI
            Log.d(TAG, "Identificador original: '$userIdentifier'")


            // 2. Codifica a string usando a nossa função customizada.
            // O resultado será um payload de no máximo 18 bytes.
            val encodedPayload = encodeCustom(userIdentifier)
            Log.d(TAG, "Payload codificado (Hex): ${encodedPayload.toHexString()} (${encodedPayload.size} bytes)")

            // 3. Anexa o Status Word de sucesso (0x9000) e retorna a resposta final.
            return encodedPayload + SW_OK

            //FIM DA LÓGICA DE CODIFICAÇÃO CUSTOMIZADA

        } else {
            Log.w(TAG, "Comando desconhecido recebido.")
            return SW_UNKNOWN_COMMAND
        }
    }

    /**
     * Chamado quando a conexão NFC é perdida.
     */
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Desativado: Razão -> $reason")
    }
}



//FUNÇÕES DE CODIFICAÇÃO/DECODIFICAÇÃO ADICIONADAS FORA DA CLASSE

// ALFABÉTO SÃO AS POSSIBILIDADES PERMITIDAS PARA O PASSAPORTE
private const val ALFABETO = "abcdefghijklmnopqrstuvwxyz._-"

// ASSOCIA O CARACTERE AO INDICE NA ORDEM APRESENTADA: a = 0, b = 1, .... 
private val CHAR_TO_INT_MAP = ALFABETO.withIndex().associate { (index, char) -> char to index }

/**
 * Codifica uma string de ATÉ 27 caracteres usando o alfabeto customizado de 5 bits.
 */
fun encodeCustom(input: String): ByteArray {
    require(input.length <= 27) { "A entrada não pode exceder 27 caracteres." }

    val textoValido = input.filter { it in ALFABETO }
    val bitStringBuilder = StringBuilder()

    for (char in textoValido) {
        val index = CHAR_TO_INT_MAP[char]!!
        bitStringBuilder.append(Integer.toBinaryString(index).padStart(5, '0'))
    }

    // TRANSFORMA BITS EM BYTES
    val bits = bitStringBuilder.toString()

    val numBytes = (bits.length + 7) / 8
    val dataBytes = ByteArray(numBytes)
    for (i in 0 until numBytes) {
        val end = minOf((i + 1) * 8, bits.length)
        val byteString = bits.substring(i * 8, end).padEnd(8, '0')
        dataBytes[i] = byteString.toInt(2).toByte()
    }

    // MONTAGEM DO CÓDIGO FINAL
    val payload = ByteArray(1 + dataBytes.size)
    payload[0] = textoValido.length.toByte()
    System.arraycopy(dataBytes, 0, payload, 1, dataBytes.size)
    return payload
}


// Decodifica os bytes de volta para a string original.
 
fun decodeCustom(payload: ByteArray): String {
    if (payload.isEmpty()) return ""
    val originalLength = payload[0].toInt()
    if (originalLength == 0) return ""

    val dataBytes = payload.copyOfRange(1, payload.size)
    val bitStringBuilder = StringBuilder()

    for (byte in dataBytes) {
        bitStringBuilder.append(
            Integer.toBinaryString(byte.toInt() and 0xFF).padStart(8, '0')
        )
    }
    val bits = bitStringBuilder.toString()
    val result = StringBuilder()

    for (i in 0 until originalLength) {
        val start = i * 5
        val end = start + 5
        if (end > bits.length) break

        val bitChunk = bits.substring(start, end)
        val index = bitChunk.toInt(2)
        if (index < ALFABETO.length) {
            result.append(ALFABETO[index])
        }
    }
    return result.toString()
}


fun ByteArray.toHexString(): String = joinToString(separator = " ") { "%02X".format(it) }
