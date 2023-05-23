package login

import com.catan.sdk.dto.login.LoginDto
import com.catan.sdk.dto.login.LoginSuccessDto
import com.catan.sdk.toDto
import database.DatabaseService
import error.WrongPassword
import error.WrongUserName
import session.SessionService
import socket.SocketConnection

class LoginService(
        private val dbService: DatabaseService,
        private val sessionService: SessionService
) {
    fun login(message: String, socket: SocketConnection): LoginSuccessDto {
        val loginDto: LoginDto = message.toDto()

        val listOfUsers = dbService.getUserByusername(loginDto.username)
        if (listOfUsers.size != 1) {
            throw WrongUserName()
        }
        if (listOfUsers[0].password != loginDto.password) {
            throw WrongPassword()
        }
        println("logged in")
        return LoginSuccessDto(
            sessionService.addUser(loginDto.username, socket)
        )
    }
}
