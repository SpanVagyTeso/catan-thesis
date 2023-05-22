package socket

import com.catan.sdk.dto.error.BadDto
import com.catan.sdk.exception.BadDtoRuntimeException
import com.catan.sdk.toJson
import error.CommonError
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.File

class SocketController(
    private val socketService: SocketService,
    private val port: Int
) {
    fun startController() {
        val cfg = File("build/resources/serverConfig.txt")
        var port = 0
        cfg.readLines()
        cfg.forEachLine {
            if(it.startsWith("port=")){
                port = it.removePrefix("port=").toInt()
            }
        }
        runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)
            val serverSocket = aSocket(selectorManager).tcp()
                .bind("localhost", port)
            println("Server is listening at ${serverSocket.localAddress}")
            while (true) {
                val socket = serverSocket.accept()
                println("Socket accepted: $socket")
                launch {
                    SocketConnection(socket, socketService).receive()
                }
            }
        }
    }

}

class SocketConnection(
    private val socket: Socket,
    private val socketService: SocketService,
) {
    private val receiveChannel: ByteReadChannel = socket.openReadChannel()
    private val sendChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)

    suspend fun receive() = coroutineScope {
        while (!socket.isClosed) {
            try {
                val message = receiveChannel.readUTF8Line()
                if (message == null) {
                    withContext(Dispatchers.IO) {
                        socket.close()
                    }
                    break
                }
                println("Message received $message")
                socketService.handleIncomingMessage(
                    this@SocketConnection,
                    message
                )

            } catch (e: CommonError) {
                e.printStackTrace()
                sendMessage(
                    BadDto("bad dto").toJson()
                )
            } catch (e: BadDtoRuntimeException) {
                e.printStackTrace()
                sendMessage(
                    BadDto("bad dto").toJson()
                )
            } catch (e: Throwable) {
                println("Closing: $socket")
                e.printStackTrace()
                withContext(Dispatchers.IO) {
                    socket.close()
                }
            }
        }
        socketService.closedConnection(this@SocketConnection)
    }

    suspend fun sendMessage(msg: String) {
        if(sendChannel.isClosedForWrite) return
        println("Message: $msg")
        println("Is closed? :${sendChannel.isClosedForWrite}")
        sendChannel.writeStringUtf8(msg + "\n")

    }

}
