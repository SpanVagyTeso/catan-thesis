import com.catan.sdk.dto.login.LoginDto
import com.catan.sdk.toDto
import com.catan.sdk.toJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DtoTest : FunSpec({
    test("data to json") {
        val ldto = LoginDto("alma", "alma")
        println(ldto.toJson())
        ldto shouldBe ldto.toJson().toDto()
    }
})
