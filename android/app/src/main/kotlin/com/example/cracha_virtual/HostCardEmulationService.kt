// Define o pacote ao qual este arquivo pertence. Deve corresponder à estrutura de pastas do seu projeto.
package com.example.cracha_virtual // Use seu pacote correto

// Importa a classe base do Android para criar um serviço de emulação de cartão (HCE).
import android.nfc.cardemulation.HostApduService
// Usado para passar dados extras, embora não seja utilizado ativamente neste exemplo.
import android.os.Bundle
// Ferramenta de log do Android para depuração.
import android.util.Log

/**
 * Esta classe é o coração da emulação de cartão NFC.
 * Ela herda de 'HostApduService', uma classe do Android feita especificamente para isso.
 * O sistema operacional Android direciona automaticamente os comandos NFC (APDUs) do leitor
 * para esta classe quando o app está em primeiro plano.
 */
class HostCardEmulationService : HostApduService() {

    // 'companion object' é semelhante a membros estáticos em Java.
    // As constantes definidas aqui são acessíveis a partir da classe sem precisar criar uma instância.
    companion object {
        // TAG para filtrar mensagens no Logcat, facilitando a depuração.
        const val TAG = "HostCardEmulationService"

        // APDU (Application Protocol Data Unit) é o formato de mensagem trocado entre um cartão e um leitor.
        // Este é o comando "SELECT AID" que o leitor de NFC (a fechadura) envia para encontrar o nosso "cartão".
        // Estrutura do comando:
        // CLA | INS | P1 | P2 | Lc  | Data (Nosso AID)
        // 00  | A4  | 04 | 00 | 07  | F0A1B2C3D4E5F6
        val SELECT_APDU = byteArrayOf(
            0x00.toByte(), // CLA (Classe do comando) - Padrão para muitas aplicações.
            0xA4.toByte(), // INS (Código da instrução) - 0xA4 é o código padrão para "SELECT".
            0x04.toByte(), // P1 (Parâmetro 1) - Indica seleção por nome (AID).
            0x00.toByte(), // P2 (Parâmetro 2) - Informações adicionais, 00 é comum.
            0x07.toByte(), // Lc (Length of command data) - O tamanho do nosso AID em bytes (7 bytes).
            // Data: O nosso Application ID (AID). DEVE ser o mesmo definido no arquivo 'apduservice.xml'.
            0xF0.toByte(), 0xA1.toByte(), 0xB2.toByte(), 0xC3.toByte(), 0xD4.toByte(), 0xE5.toByte(), 0xF6.toByte()
        )

        // Status Words (SW): Respostas de 2 bytes que o cartão envia de volta ao leitor.
        // 0x9000 (SW_OK) é o código universal para "Comando executado com sucesso".
        val SW_OK = byteArrayOf(0x90.toByte(), 0x00.toByte())
        // Código de erro para quando o comando recebido não é o esperado.
        val SW_UNKNOWN_COMMAND = byteArrayOf(0x6D.toByte(), 0x00.toByte())
        // Outro código de erro para comandos nulos/inválidos.
        val SW_UNKNOWN_COMMAND2 = byteArrayOf(0xA1.toByte(), 0x00.toByte())
    }

    /**
     * Este é o método MAIS IMPORTANTE da classe.
     * Ele é chamado pelo sistema Android toda vez que um leitor NFC envia um comando (APDU) para o dispositivo.
     *
     * @param commandApdu O comando enviado pelo leitor, como um array de bytes.
     * @param extras Informações adicionais do sistema (não usado aqui).
     * @return A resposta que queremos enviar de volta ao leitor, também como um array de bytes.
     */
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray? {
        // Primeiro, verifica se o comando recebido não é nulo.
        if (commandApdu == null) {
            return SW_UNKNOWN_COMMAND2 // Se for nulo, retorna um erro.
        }

        // Usando a função de extensão abaixo para imprimir o comando recebido de forma legível no Logcat.
        Log.d(TAG, "Comando APDU recebido: ${commandApdu.toHexString()}")

        // Compara o comando recebido do leitor com o comando "SELECT_APDU" que esperamos.
        if (commandApdu.contentEquals(SELECT_APDU)) {
            // Se o comando for o correto, significa que o leitor nos encontrou!
            Log.i(TAG, "Comando SELECT AID correto. Respondendo com sucesso.")
            
            // Aqui você pode enviar uma resposta customizada junto com o status de sucesso.
            val responseMessage = "ACESSO_LIBERADO"
            
            // Concatena a nossa mensagem de resposta com o código de status "OK".
            // O leitor receberá "ACESSO_LIBERADO" e saberá que o comando foi bem-sucedido.
            return responseMessage.toByteArray(Charsets.UTF_8) + SW_OK
        } else {
            // Se o leitor enviou um comando que não reconhecemos.
            Log.w(TAG, "Comando desconhecido recebido.")
            // Responde com um código de erro para informar ao leitor que não entendemos o comando.
            return SW_UNKNOWN_COMMAND
        }
    }

    /**
     * Este método é chamado quando a conexão NFC é perdida.
     * (Ex: o usuário afastou o celular do leitor).
     *
     * @param reason O motivo da desativação (ex: LINK_LOSS, DESELECTED).
     */
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Desativado: Razão -> $reason")
    }
}

/**
 * Esta é uma "função de extensão" em Kotlin.
 * Ela adiciona a função 'toHexString()' a qualquer objeto do tipo 'ByteArray'.
 * É uma forma muito útil de converter dados binários em texto legível para depuração.
 * Exemplo: [0xF0, 0xA1] se torna "F0 A1".
 */
fun ByteArray.toHexString(): String = joinToString(separator = " ") { "%02X".format(it) }