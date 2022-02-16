package asio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

fun main() {
    SocketServer()
}

class SocketServer {
    private var channel: AsynchronousServerSocketChannel = AsynchronousServerSocketChannel.open()

    init {
        println("Opening channel.")
        channel.bind(InetSocketAddress("127.0.0.1", 4555))

        println("Creating client handler.")
        channel.accept(null, object: CompletionHandler<AsynchronousSocketChannel, Any?> {
            override fun completed(connection: AsynchronousSocketChannel?, attachment: Any?) {
                if (channel.isOpen) {
                    channel.accept<Any?>(null, this) //accept new clients connecting
                }

                if ((connection != null) && connection.isOpen) {
                    val handler = ReadWriteHandler(connection) // added clientChannel - has to be local in instance
                    val buffer = ByteBuffer.allocate(32)
                    val readInfo: MutableMap<String, Any> = HashMap()
                    readInfo["action"] = "read"
                    readInfo["buffer"] = buffer
                    connection.read(buffer, readInfo, handler) //handler is used for communication with client
                }
            }

            override fun failed(exc: Throwable?, attachment: Any?) {
                TODO("Not yet implemented")
            }
        })

        println("Press enter to stop server...")
        System.`in`.read()
    }

    internal inner class ReadWriteHandler(private val clientChannel: AsynchronousSocketChannel): CompletionHandler<Int, MutableMap<String, Any>> {
        /* Tomas, keep clientChannel local, ie. for this client */
        override fun completed(result: Int, attachment: MutableMap<String, Any>) {
            println()
            println("Got a connection.")
            val action = attachment["action"] as String?
            println("The action is $action")

            //check if client has closed socket channel
            if (result == -1) {
                println("Client closed connection.")
                return
            }

            if (action == "read") {
                val buffer = attachment["buffer"] as ByteBuffer
                buffer.flip()
                attachment["action"] = "write"
                clientChannel.write(buffer, attachment, this)
                buffer.clear()
                println("Registered new callback/listener for clientChannel.write()")
            } else if (action == "write") {
                val buffer = ByteBuffer.allocate(32)
                attachment["action"] = "read"
                attachment["buffer"] = buffer
                clientChannel.read(buffer, attachment, this)
                println("Registered new callback/listener for clientChannel.read()")
            }
        }

        override fun failed(exc: Throwable?, attachment: MutableMap<String, Any>?) {
            TODO("Not yet implemented")
        }
    }
}