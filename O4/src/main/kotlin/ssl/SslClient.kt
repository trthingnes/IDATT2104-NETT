package ssl

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.util.*
import javax.net.ssl.SSLSocketFactory

fun main() {
    SslClient()
}

/*
 ! Has to run with VM options:
 ! -Djavax.net.ssl.trustStore=$MODULE_DIR$/../../keystore/examplestore -Djavax.net.ssl.trustStorePassword=password
 */
class SslClient {
    private val port = 8000

    init {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = factory.createSocket("localhost", port)
        val out = PrintWriter(socket.getOutputStream(), true)

        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
            val scanner = Scanner(System.`in`)
            while (true) {
                println("Enter something:")
                val inputLine: String = scanner.nextLine()
                if (inputLine == "q") {
                    break
                }
                out.println(inputLine)
                println(reader.readLine())
            }
        }
    }
}