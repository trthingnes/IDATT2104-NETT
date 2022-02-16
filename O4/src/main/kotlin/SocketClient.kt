import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel


class AsyncSocketClient {
    private var client: AsynchronousSocketChannel? = null
    @Throws(Exception::class)
    fun init() {
        client = AsynchronousSocketChannel.open()
        val hostAddress = InetSocketAddress("localhost", 4555)
        val future = client.connect(hostAddress)
        future.get() //wait for connection
    }

    @Throws(Exception::class)
    fun sendMessage(message: String?): String {
        val byteMsg: ByteArray = String(message).toByteArray()
        val buffer: ByteBuffer = ByteBuffer.wrap(byteMsg)
        val writeResult = client!!.write(buffer)

        // do some computation
        writeResult.get() //wait for result
        buffer.flip()
        val readResult = client!!.read(buffer)

        // do some computation
        readResult.get() //wait for result
        val echo: String = String(buffer.array()).trim { it <= ' ' }
        buffer.clear()
        return echo
    }

    @Throws(IOException::class)
    fun cleanUp() {
        client!!.shutdownInput()
        client!!.shutdownOutput()
        client!!.close()
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            println("*Start client")
            val client = AsyncSocketClient()
            client.init()
            val serverString = client.sendMessage("TEST")
            println("Message for server $serverString")
            //serverString = client.sendMessage("TEST 2**");
            //System.out.println("Message 2 from server " + serverString);
            client.cleanUp()
        }
    }
}