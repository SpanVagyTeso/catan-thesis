package error

open class CommonError(message: String) : Exception(message)

class WrongUserName : CommonError("wrong username")

class WrongPassword : CommonError("wrong password")

class UserAlreadyExists : CommonError("user already exists")

class InvalidSessionId : CommonError("invalid session id")

class InvalidDto : CommonError("invalid dto")

class InvalidLobbyId : CommonError("invalid lobby id")

class LobbyFull : CommonError("lobby is full")

class LobbyIsStarted : CommonError("lobby is already started")

class NotOwner : CommonError("you arent the owner of the lobby")
