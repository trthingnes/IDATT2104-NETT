import java.io.*
import java.net.ServerSocket
import java.net.Socket

fun main() {
    SocketServer(1200) {
        val reader = SocketServer.makeReader(it)
        val writer = SocketServer.makeWriter(it)

        writer.println("Hello, type something for me to repeat:")

        var line = reader.readLine()
        while (line != null) {
            writer.println("You typed: $line")
            line = reader.readLine()
        }

        it.close()
    }
}

class SocketServer(port: Int, handler: (Socket) -> Unit) {
    init {
        val server = ServerSocket(port)

        println("Waiting for connection...")

        while (true) {
            val connection = server.accept()
            println("Got a connection from ${connection.inetAddress}:${connection.port}.")
            Thread { handler(connection) }.start()
        }
    }

    companion object {
        fun makeReader(c: Socket) = BufferedReader(InputStreamReader(c.getInputStream()))
        fun makeWriter(c: Socket) = PrintWriter(c.getOutputStream(), false)
    }

}