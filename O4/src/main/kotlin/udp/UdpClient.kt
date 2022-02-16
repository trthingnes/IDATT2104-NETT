package udp

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

fun main() {
    val client = UdpClient()
    val scanner = Scanner(System.`in`)

    println(client.send("Hello from client!"))

    while (true) {
        println(client.send(scanner.nextLine()))
    }
}

class UdpClient {
    private val socket = DatagramSocket()
    private val address = InetAddress.getByName("localhost")

    fun send(message: String): String {
        val bufOut = message.toByteArray()
        val outbound = DatagramPacket(bufOut, bufOut.size, address, 4445)
        socket.send(outbound)

        val bufIn = ByteArray(256)
        val inbound = DatagramPacket(bufIn, bufIn.size)
        socket.receive(inbound)

        return getPacketString(inbound)
    }

    private fun getPacketString(packet: DatagramPacket): String {
        return String(packet.data, 0, packet.length)
    }
}