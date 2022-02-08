import java.io.*
import java.net.ServerSocket
import java.net.Socket

fun main() {
    /*SocketServer(1200) {
        val reader = SocketServer.makeReader(it)
        val writer = SocketServer.makeWriter(it)

        writer.println("Hello, type something for me to repeat:")

        var line = reader.readLine()
        while (line != null) {
            writer.println("You typed: $line")
            line = reader.readLine()
        }

        it.close()
    }*/

    SocketServer(1200) {
        val reader = SocketServer.makeReader(it)
        val writer = SocketServer.makeWriter(it)
        var running = true

        while(running) {
            writer.println("Hello, I will do your math! Type your operator:")
            var line = reader.readLine()

            while (!line.equals("+") && !line.equals("-")) {
                writer.println("I didn't recognize that, type '+' or '-'.")
                line = reader.readLine()
            }

            val plus = line.equals("+")

            val numbers = arrayListOf<Int>()
            while (numbers.size < 2) {
                writer.println("Type a number:")
                line = reader.readLine()

                try {
                    numbers.add(Integer.parseInt(line))
                }
                catch(_: Exception) {/* empty */}
            }

            writer.println("Thanks! Your result: " +
                    "${numbers[0]} " +
                    "${if (plus) "+" else "-"} " +
                    "${numbers[1]} " +
                    "= ${ if (plus) (numbers[0] + numbers[1]) else (numbers[0] - numbers[1]) }" +
                    " | Press enter to quit, type something to continue."
            )
            running = reader.readLine() != null
        }

        reader.close()
        writer.close()
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
        fun makeWriter(c: Socket) = PrintWriter(c.getOutputStream(), true)
    }

}