import com.catan.sdk.entities.Map
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BasicTest : FunSpec({

    test("can join all tiles, vertexes, edges") {
        val map1 = Map()
        val map2 = Map()

        map1.generateTiles()
        map2.generateTiles()
        map1.attachAllTiles()
        map2.attachAllTiles()

        val tile1 = map1.tiles.find {
            it.id == "T17"
        }!!
        val tile2 = map2.tiles.find {
            it.id == "T17"
        }!!


        tile2.vertices.map {
            it!!.id
        }.toSet() shouldBe tile1.vertices.map {
            it!!.id
        }.toSet()

        tile2.vertices.find {
            it!!.id == "V42"
        }!!.edges.map {
            it.id
        } shouldBe tile1.vertices.find {
            it!!.id == "V42"
        }!!.edges.map {
            it.id
        }

        tile2.vertices.find {
            it!!.id == "V42"
        }!!.edges.map {
            it.id
        }.toSet().count() shouldBe 3

    }
})
