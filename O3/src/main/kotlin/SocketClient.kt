import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*

fun main() {
    SocketClient("127.0.0.1", 1200)
}

class SocketClient(host: String, port: Int) {
    init {
        val scanner = Scanner(System.`in`)
        val connection = Socket(host, port)
        val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
        val writer = PrintWriter(connection.getOutputStream(), true)

        println(reader.readLine())

        var line = scanner.nextLine()
        while(line.isNotEmpty()) {
            writer.println(line)
            println("Respons: '${reader.readLine()}'")
            line = scanner.nextLine()
        }

        reader.close()
        writer.close()
        connection.close()
    }


}