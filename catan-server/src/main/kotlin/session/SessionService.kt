package session

import com.catan.sdk.entities.Player
import kotlinx.coroutines.runBlocking
import socket.SocketConnection
import java.time.Instant

class SessionService {

    private val sessionIds = mutableMapOf<String, SessionInfo>()

    private fun createSessionId(username: String): String = "CATAN" + username + Instant.now().epochSecond

    fun addUser(username: String, socket: SocketConnection): String {
        val sessionId = createSessionId(username)
        sessionIds[username] = SessionInfo(
            socket,
            username,
            sessionId
        )
        return sessionId
    }

    fun getUserFromSessionId(session: String): String? {
        sessionIds.forEach {
            if (it.value.sessionId == session)
                return it.key
        }
        return null
    }

    fun getSessionInfoFromUsername(username: String): SessionInfo {
        return sessionIds[username]!!
    }

    fun validateSessionId(sessionId: String) =
        sessionIds.values.filter {
            it.sessionId == sessionId
        }.size == 1

    fun getSessionIdForUser(username: String) = sessionIds[username]

    fun sendDatas(dataToSend: List<Pair<Player, String>>) {
        runBlocking {
            dataToSend.forEach { (player, data) ->
                sessionIds[player.username]?.socket!!.sendMessage(
                    data
                )
            }
        }
    }

    fun getUsernameFromSocket(socket: SocketConnection) = sessionIds.values.find {
        it.socket == socket
    }

}

data class SessionInfo(
    val socket: SocketConnection,
    val username: String,
    val sessionId: String
)
