package asio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

fun main() {
    AsioClient("Hello there!")
}

class AsioClient(msg: String) {
    private val socket = InetSocketAddress("127.0.0.1", 4555)
    private val client = AsynchronousSocketChannel.open()
    private val future = client.connect(socket)

    init {
        println("Connecting to host...")
        future.get()
        println("Connected.")

        val buffer = ByteBuffer.wrap(msg.toByteArray())
        val writeResult = client.write(buffer)
        // * We can do other stuff here.
        writeResult.get()

        buffer.flip()

        // Fit buffer to length of String.

        val readResult = client.read(buffer)
        // * We can do other stuff here.
        readResult.get()

        println("Got response: '${String(buffer.array())}'.")
    }
}