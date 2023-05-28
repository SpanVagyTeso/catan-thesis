package game

import com.catan.sdk.entities.Player
import com.catan.sdk.entities.PlayerColor
import com.catan.sdk.entities.PlayerColor.RED
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAllKeys
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class GameTest : FunSpec({
    test("Should create a game") {
        val players = BasicGameSetup().also { it.giveAlLResources( 5) }
        val game = Game(
            players.toList(),
            mockk(),
            mockk()
        )
        game.atBeginning shouldBe true
    }
})

data class BasicGameSetup(
    val alma: Player = Player("alma", RED),
    val korte: Player = Player("korte", RED),
    val barack: Player = Player("barack", RED),
    val narancs: Player = Player("narancs", RED),
){
    fun giveAlLResources(number: Int){
        alma.resources.forAllKeys {
            alma.resources[it] = number
            korte.resources[it] = number
            barack.resources[it] = number
            narancs.resources[it] = number
        }
    }
    fun toList() = listOf(alma, korte, barack, narancs)
}
