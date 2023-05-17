package game

import com.catan.sdk.entities.DevelopmentTypes
import com.catan.sdk.entities.DevelopmentTypes.*

open class DevelopmentPool(
    knight: Int = 0,
    monopoly: Int = 0,
    yearOfPlenty: Int = 0,
    roadBuilding: Int = 0,
    greatHall: Int = 0,
    chapel: Int = 0,
    university: Int = 0,
    market: Int = 0,
    library: Int = 0
) {
    private var pool = mutableListOf<DevelopmentTypes>()

    init {
        pool += List(knight) { Knight }
        pool += List(monopoly) { Monopoly }
        pool += List(yearOfPlenty) { YearOfPlenty }
        pool += List(roadBuilding) { RoadBuilding }
        pool += List(greatHall) { GreatHall }
        pool += List(chapel) { Chapel }
        pool += List(university) { University }
        pool += List(market) { Market }
        pool += List(library) { Library }
        pool.shuffle()
    }

    open fun draw() = pool.removeFirstOrNull()

    fun remaining() = pool.count()
}
