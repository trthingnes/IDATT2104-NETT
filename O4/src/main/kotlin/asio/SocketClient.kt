package asio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

fun main() {
    println("Start client")
    val client = SocketClient()
    val serverString = client.sendMessage("TEST")
    println("Message for server $serverString")
    client.quit()
}

class SocketClient {
    private val client = AsynchronousSocketChannel.open()
    private val address = InetSocketAddress("localhost", 4555)

    init {
        // Wait for connection.
        client.connect(address).get()
    }

    fun sendMessage(message: String): String {
        val bytes = message.toByteArray()
        val buffer = ByteBuffer.wrap(bytes)
        val writeResult = client.write(buffer)

        // TODO

        writeResult.get()
        buffer.flip()
        val readResult = client.read(buffer)

        // TODO

        readResult.get()

        return String(buffer.array()).trim { it <= ' ' }
    }

    fun quit() {
        client.shutdownInput()
        client.shutdownOutput()
        client.close()
    }
}