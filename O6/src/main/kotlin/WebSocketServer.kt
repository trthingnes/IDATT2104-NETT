import java.io.BufferedReader
import java.net.ServerSocket
import java.security.MessageDigest
import java.util.*


fun main() {
    WebSocketServer(8888)
}

class WebSocketServer(port: Int) {
    init {
        val socket = ServerSocket(port)

        while (true) {
            val client = socket.accept()
            println("Got a connection from :${client.port}.")

            val input = Scanner(client.getInputStream(), "UTF-8")
            input.useDelimiter("\\r\\n\\r\\n")

            val request = parseRequest(input.next())

            val output = client.getOutputStream()
            val response = generateResponse(request).toByteArray()
            output.write(response, 0, response.size)
        }
    }

    private fun parseRequest(request: String): Request {
        return Request(request.split("\\r\\n"))
    }

    private fun generateResponse(request: Request): String {
        val key = request.getAttributeValue("Sec-WebSocket-Key")
        val accept = generateAccept(key)

        return "HTTP/1.1 101 Web Socket Protocol Handshake\r\n" +
               "Upgrade: WebSocket\r\n" +
               "Connection: Upgrade\r\n" +
               "Sec-WebSocket-Accept: $accept\r\n"
    }

    private fun generateAccept(key: String): String {
        val guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val hash = MessageDigest.getInstance("SHA-1").digest((key + guid).toByteArray())

        return Base64.getEncoder().encodeToString(hash)
    }

    private fun isWebSocketRequest(request: Request): Boolean {
        val correctConnectionType = request.isAttributeValue("Connection", "Upgrade")
        val correctUpgradeType = request.isAttributeValue("Upgrade", "websocket")

        return correctConnectionType && correctUpgradeType
    }

    class Request(data: List<String>) {
        private val attributes: HashMap<String, String> = hashMapOf()
        private var type: String = ""

        init {
            data.map {
                if(data.indexOf(it) != 0) {
                    type = it
                }
                else {
                    val list = it.split(": ")
                    attributes[list[0]] = list[1]
                }
            }
        }

        fun isAttributePresent(name: String): Boolean {
            return attributes.containsKey(name)
        }

        fun isAttributeValue(name: String, value: String): Boolean {
            return attributes.containsKey(name) && attributes[name].equals(value)
        }

        fun getAttributeValue(name: String): String {
            return attributes[name] ?: ""
        }
    }
}