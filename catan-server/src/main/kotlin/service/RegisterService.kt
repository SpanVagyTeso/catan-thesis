package service

import com.catan.sdk.dto.register.RegisterDto
import com.catan.sdk.dto.register.RegisterSuccessDto
import com.catan.sdk.toDto
import database.User

class RegisterService(
    private val databaseService: DatabaseService
) {
    //REGISTER/username/password
    fun register(message: String): RegisterSuccessDto {
        with(message.toDto<RegisterDto>()) {
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
