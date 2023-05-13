package controller

import com.catan.sdk.dto.DtoType
import com.catan.sdk.toJson
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

class SocketController {
    lateinit var socket: Socket
    lateinit var receiveChannel: ByteReadChannel
    lateinit var sendChannel: ByteWriteChannel
    lateinit var handler: (String) -> Unit

    fun run() {
        val selectorManager = SelectorManager(Dispatchers.IO)
        try {
            runBlocking {
                socket = aSocket(selectorManager).tcp().connect("localhost", 8000)

                receiveChannel = socket.openReadChannel()
                sendChannel = socket.openWriteChannel(autoFlush = true)

                while (true) {
                    val receivedMessage = receiveChannel.readUTF8Line()
                    if (receivedMessage != null) {
                        launch { handler(receivedMessage) }

                    } else {
                        println("Server closed a connection")
                        socket.close()
                        selectorManager.close()
                        exitProcess(0)
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendDto(dto: DtoType) {
        runBlocking {
            launch(Dispatchers.IO) {
                println("Sending: ${dto.toJson()}")
                sendChannel.writeStringUtf8("${dto.toJson()}\n")
            }
        }
    }
}
