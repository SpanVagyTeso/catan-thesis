package database

import error.UserAlreadyExists
import org.hibernate.Session
import org.hibernate.cfg.Configuration

class DatabaseService {

    var session: Session

    init {
        val con = Configuration().configure().addAnnotatedClass(User::class.java)
        session = con.buildSessionFactory().openSession()
    }


    fun getUserByUsername(userName: String): MutableList<User> {
        val builder = session.criteriaBuilder
        val criteria = builder.createQuery(User::class.java)
        val root = criteria.from(User::class.java)
        criteria.where(builder.like(root.get("userName"), userName))
        return session.createQuery(criteria).resultList
    }

    fun saveUser(user: User) {
        if (getUserByUsername(user.userName).size != 0) {
            throw UserAlreadyExists()
        }
        transaction {
            session.persist(user)
        }
    }

    fun updateUser(user: User) {
        transaction {
            session.merge(user)
        }
    }

    private fun transaction(actual: () -> Unit) {
        val transaction = session.beginTransaction()
        actual()
        transaction.commit()
    }
}
