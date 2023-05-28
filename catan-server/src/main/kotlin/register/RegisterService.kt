package register

import com.catan.sdk.dto.register.RegisterDto
import com.catan.sdk.dto.register.RegisterSuccessDto
import com.catan.sdk.toDto
import database.DatabaseService
import database.User
import error.WrongPassword
import error.WrongUserName

class RegisterService(
    private val databaseService: DatabaseService
) {
    fun register(message: String): RegisterSuccessDto {
        with(message.toDto<RegisterDto>()) {
            if(!password.contains(Regex("[a-zA-Z0-9]"))) throw WrongPassword()
            if(databaseService.getUserByUsername(username).size > 0) throw WrongUserName()
            databaseService.saveUser(
                User().apply {
                    userName = this@with.username
                    password = this@with.password
                }
            )
            return RegisterSuccessDto(true)
        }
    }
}
