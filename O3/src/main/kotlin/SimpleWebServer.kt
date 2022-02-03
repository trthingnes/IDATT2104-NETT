fun main() {
    SocketServer(8080) {
        val reader = SocketServer.makeReader(it)
        val writer = SocketServer.makeWriter(it)

        writer.println("HTTP/1.0 200 OK")
        writer.println("Content-Type: text/html; charset=utf-8\n")
        writer.println("<html><body><h1>Hello there!</h1>")

        writer.println("<h3>Your request lines</h3><ul>")
        var line = reader.readLine()
        while (!line.isNullOrBlank()) {
            writer.println("<li>$line</li>")
            line = reader.readLine()
        }
        writer.println("</ul></body></html>")

        writer.close()
        it.close()
    }
}