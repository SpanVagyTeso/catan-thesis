package game

import kotlin.random.Random

class DiceRoller(
    val sizeOfDice1: Int = 6,
    val sizeOfDice2: Int = 6,
) {

    fun rollTheDice() = (Random.nextInt(0, sizeOfDice1) + 1) to (Random.nextInt(0, sizeOfDice2) + 1)

}
