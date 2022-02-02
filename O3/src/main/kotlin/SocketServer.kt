import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

fun main() {
    SocketServer(1200) { it }
}

class SocketServer(port: Int, handler: (String) -> String) {
    private val port = 1200

    init {
        val server = ServerSocket(port)

        val connection = server.accept()
        val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
        val writer = PrintWriter(connection.getOutputStream(), true)

        writer.println("Hei! Skriv det du vil jeg skal gjenta:")

        var line = reader.readLine()
        while(line != null) {
            print("En klient skrev '$line'.")
            writer.println("Du skrev '$line'.")
            line = reader.readLine()
        }
    }
}