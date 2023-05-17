package database

import jakarta.persistence.*

@Entity
@Table(name = "user_datas")
@SequenceGenerator(
    name = "user_datas_id_seq",
    sequenceName = "user_datas_id_seq",
    allocationSize = 1
)
class User {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "user_datas_id_seq"
    )
    val id: Long? = null

    @Column(
        nullable = false
    )
    lateinit var userName: String

    @Column(
        nullable = false
    )
    lateinit var password: String

    @Column(
        nullable = false
    )
    var gamesPlayed: Int = 0

    @Column(
        nullable = false
    )
    var gamesWon: Int = 0
}
