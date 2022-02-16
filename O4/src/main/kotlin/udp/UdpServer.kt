package udp

import java.lang.IllegalStateException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun main() {
    UdpServer().run()
}

class UdpServer {
    private val socket = DatagramSocket(4445)
    private var address = InetAddress.getLocalHost()
    private var port = 0

    private val numbers = arrayListOf<Int>()
    private var operator = ""

    fun run() {
        while(true) {
            val buffer = ByteArray(256)
            val packet = DatagramPacket(buffer, buffer.size)

            println("Waiting for initial packet.")
            socket.receive(packet) // Receive first packet.
            println("Handling packets from ${packet.address}:${packet.port}.")
            handle(packet)
        }
    }

    private fun handle(packet: DatagramPacket) {
        address = packet.address
        port = packet.port
        println("First packet contained: '${getPacketString(packet)}'.")

        while(true) {
            val message = getMessageString()

            val bufOut = message.toByteArray()
            val outgoing = DatagramPacket(bufOut, bufOut.size, address, port)
            socket.send(outgoing)

            val bufIn = ByteArray(256)
            val incoming = DatagramPacket(bufIn, bufIn.size)
            socket.receive(incoming)

            handleInput(getPacketString(incoming))

            println("Received packet containing: '${getPacketString(incoming)}'.")
        }
    }

    private fun handleInput(input: String) {
        when {
            numbers.size < 2 -> numbers.add(Integer.parseInt(input))
            operator.isBlank() -> operator = input
            else -> throw IllegalStateException("All input has already been given.")
        }
    }

    private fun getMessageString(): String {
        return when {
            numbers.size < 2 -> "Write a number (signed int): "
            operator.isBlank() -> "Write an operator (plus/+ or minus/-): "
            else -> getEquationString()
        }
    }

    private fun getEquationString(): String {
        val plus = (operator in arrayListOf("plus", "+"))
        return "${numbers[0]} ${ if (plus) "+" else "-"  } ${numbers[1]} = ${ if (plus) numbers[0] + numbers[1] else numbers[0] - numbers[1] }"
    }

    private fun getPacketString(packet: DatagramPacket): String {
        return String(packet.data, 0, packet.length)
    }
}