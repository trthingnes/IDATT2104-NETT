package asio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset
import java.util.concurrent.Future

fun main() {
    AsioServer()
}

class AsioServer {
    private val socket = InetSocketAddress("127.0.0.1", 4555)
    private val channel = AsynchronousServerSocketChannel.open().bind(socket)
    private var future: Future<AsynchronousSocketChannel> = channel.accept()

    init {
        while(channel.isOpen) {
            println("Waiting for connection...")
            val connection = future.get()
            println("Got a connection from ${connection.remoteAddress}.")

            val buffer = ByteBuffer.allocate(64)

            val readResult = connection.read(buffer)
            // * We can do other stuff here.
            readResult.get()

            buffer.flip() // Fit buffer to length of String.
            println("Got message '${Charset.defaultCharset().decode(buffer.asReadOnlyBuffer())}'.")
            println("Responding.")

            val writeResult = connection.write(buffer)
            // * We can do other stuff here.
            writeResult.get()

            buffer.clear()
            future = channel.accept()

            println("Closing connection.")
        }
    }
}