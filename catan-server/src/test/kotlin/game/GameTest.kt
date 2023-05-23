package game

import com.catan.sdk.entities.Player
import com.catan.sdk.entities.PlayerColor
import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAllKeys
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import database.DatabaseService

class GameTest : FunSpec({

    fun setupPlayers(): BasicGameSetup {
        val colors = PlayerColor.values().toMutableList()
        val alma = Player("alma", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val korte = Player("korte", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val barack = Player("barack", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val narancs = Player("narancs", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }

        return BasicGameSetup(
            alma,
            korte,
            barack,
            narancs
        )
    }

    test("game object should initialize") {
        val colors = PlayerColor.values().toMutableList()
        val alma = Player("alma", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val korte = Player("korte", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val barack = Player("barack", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val narancs = Player("narancs", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val game = Game(
            listOf(
                alma,
                korte,
                barack,
                narancs
            ),
            mockk(),
            mockk<DatabaseService>(),
            mockk<DiceRoller>(),
        )
        game.currentPlayer.username shouldBe alma.username
        game.atBeginning shouldBe true
    }

    test("Game should begin after everyone placing their number of villages and roads") {
        val colors = PlayerColor.values().toMutableList()
        val alma = Player("alma", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val korte = Player("korte", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val barack = Player("barack", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val narancs = Player("narancs", colors.removeFirst()).apply {
            resources.forAllKeys {
                resources[it] = 1
            }
        }
        val game = Game(
            listOf(
                alma,
                korte,
                barack,
                narancs
            ),
            mockk(),
            mockk<DatabaseService>(),
            mockk<DiceRoller>(),
            1,
            1
        )
        game.currentPlayer.username shouldBe alma.username
        game.atBeginning shouldBe true

//        game.placeAtBeginning(alma.username, BuyType.VILLAGE, "V0")
//        game.placeAtBeginning(korte.username, BuyType.VILLAGE, "V1")
//        game.placeAtBeginning(barack.username, BuyType.VILLAGE, "V2")
//        game.placeAtBeginning(narancs.username, BuyType.VILLAGE, "V3")
//
//        game.placeAtBeginning(alma.username, BuyType.ROAD, "E0")
//        game.placeAtBeginning(korte.username, BuyType.ROAD, "E1")
//        game.placeAtBeginning(barack.username, BuyType.ROAD, "E2")
//        game.placeAtBeginning(narancs.username, BuyType.ROAD, "E3")

//        game.atBeginning shouldBe false
    }

//    test("Should not allow to place road not next to own village") {
//        val alma = Player("alma").apply {
//            resources.forAllKeys {
//                resources[it] = 1
//            }
//        }
//        val korte = Player("korte").apply {
//            resources.forAllKeys {
//                resources[it] = 1
//            }
//        }
//        val barack = Player("barack").apply {
//            resources.forAllKeys {
//                resources[it] = 1
//            }
//        }
//        val narancs = Player("narancs").apply {
//            resources.forAllKeys {
//                resources[it] = 1
//            }
//        }
//        val game = Game(
//            listOf(
//                alma,
//                korte,
//                barack,
//                narancs
//            ),
//            mockk(),
//            mockk<DiceRoller>(),
//            1,
//            1
//        )
//        game.currentPlayer.username shouldBe alma.username
//        game.atBeginning shouldBe true
//
//        game.placeAtBeginning(alma.username, BuyType.VILLAGE, "V0")
//        game.placeAtBeginning(korte.username, BuyType.VILLAGE, "V1")
//        game.placeAtBeginning(barack.username, BuyType.VILLAGE, "V2")
//        game.placeAtBeginning(narancs.username, BuyType.VILLAGE, "V3")
//
//        game.placeAtBeginning(alma.username, BuyType.ROAD, "E33")
//
//        game.currentPlayer.username shouldBe alma.username
//        game.atBeginning shouldBe true
//    }
//
//    test("Should be able to buy some development and use them") {
//        val (alma, korte, barack, narancs) = setupPlayers()
//        val game = Game(
//            listOf(
//                alma,
//                korte,
//                barack,
//                narancs
//            ),
//            mockk(),
//            mockk<DiceRoller>(),
//            roadsAtBeginning = 1,
//            villagesAtBeginning = 1,
//            developmentPool = object : DevelopmentPool() {
//                val riggedList = mutableListOf(
//                    Monopoly,
//                    YearOfPlenty,
//                    Chapel
//                )
//
//                override fun draw() = riggedList.removeFirstOrNull()
//            }
//        )
//        game.currentPlayer.username shouldBe alma.username
//        game.atBeginning shouldBe true
//
//        game.placeAtBeginning(alma.username, BuyType.VILLAGE, "V0")
//        game.placeAtBeginning(korte.username, BuyType.VILLAGE, "V1")
//        game.placeAtBeginning(barack.username, BuyType.VILLAGE, "V2")
//        game.placeAtBeginning(narancs.username, BuyType.VILLAGE, "V3")
//
//        game.placeAtBeginning(alma.username, BuyType.ROAD, "E0")
//        game.placeAtBeginning(korte.username, BuyType.ROAD, "E1")
//        game.placeAtBeginning(barack.username, BuyType.ROAD, "E2")
//        game.placeAtBeginning(narancs.username, BuyType.ROAD, "E3")
//
//        game.atBeginning shouldBe false
//        game.currentPlayer.username shouldBe "alma"
//
//        ResourceType.values().forEach {
//            alma.resources[it] = 100
//        }
//
//        game.spendResources(alma.username, BuyType.UPGRADE)
//        alma.cards.count() shouldBe 1
//        game.spendResources(alma.username, BuyType.UPGRADE)
//        alma.cards.count() shouldBe 2
//        game.spendResources(alma.username, BuyType.UPGRADE)
//        alma.cards.count() shouldBe 3
//        game.spendResources(alma.username, BuyType.UPGRADE)
//        alma.cards.count() shouldBe 3
//
//        alma.resources[Grain] shouldBe 97
//        alma.resources[Ore] shouldBe 97
//        alma.resources[Wool] shouldBe 97
//
//        game.useDevelopmentCard(alma.username, Monopoly, MonopolyDto(Grain))
//
//        alma.cards.count() shouldBe 2
//        alma.resources[Grain] shouldBe 100
//
//        game.useDevelopmentCard(alma.username, YearOfPlenty, YearOfPlentyDto(Grain, Grain))
//
//        alma.cards.count() shouldBe 1
//        alma.resources[Grain] shouldBe 102
//
//        game.useDevelopmentCard(alma.username, Chapel)
//
//        alma.cards.count() shouldBe 0
//        alma.activeDevelopments.count() shouldBe 1
//
//
//    }
})

data class BasicGameSetup(
    val alma: Player,
    val korte: Player,
    val barack: Player,
    val narancs: Player
)
