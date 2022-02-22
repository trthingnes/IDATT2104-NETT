package ssl

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import javax.net.ssl.SSLServerSocketFactory

fun main() {
    SslServer()
}

/*
 ! Has to run with VM options:
 ! -Djavax.net.ssl.keyStore=$MODULE_DIR$/../../keystore/serverkeystore.jks -Djavax.net.ssl.keyStorePassword=password
 */
class SslServer {
    private val port = 8000

    init {
        val factory = SSLServerSocketFactory.getDefault() as SSLServerSocketFactory
        val sslServerSocket = factory.createServerSocket(port)
        println("SSL ServerSocket started")

        val socket = sslServerSocket.accept()
        println("ServerSocket accepted")

        val out = PrintWriter(socket.getOutputStream(), true)
        BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
                out.println(line)
            }
        }
    }
}