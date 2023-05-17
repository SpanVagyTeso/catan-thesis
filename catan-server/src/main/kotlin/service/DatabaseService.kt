package service

import database.User
import error.UserAlreadyExists
import org.hibernate.Session
import org.hibernate.cfg.Configuration
import org.junit.Ignore

class DatabaseService {

    var session: Session

    init {
        val con = Configuration().configure().addAnnotatedClass(
            User::class.java
        )
        session = con.buildSessionFactory().openSession()
    }

    @Ignore("unused")
    fun getAllUser(): MutableList<User> {
        val builder = session.criteriaBuilder
        val criteria = builder.createQuery(User::class.java)
        criteria.from(User::class.java)
        return session.createQuery(criteria).resultList
    }

    fun getUserByusername(userName: String): MutableList<User> {
        val builder = session.criteriaBuilder
        val criteria = builder.createQuery(User::class.java)
        val root = criteria.from(User::class.java)
        criteria.where(builder.like(root.get("userName"), userName))
        return session.createQuery(criteria).resultList
    }

    fun getUserByusername(user: User) = getUserByusername(user.userName)

    fun saveUser(user: User) {
        if (getUserByusername(user).size != 0) {
            throw UserAlreadyExists()
        }
        transaction {
            session.persist(user)
        }
    }

    fun updateUser(user: User){
        transaction {
            session.merge(user)
        }
    }

    private fun transaction(actual: () -> Unit) {
        val trans = session.beginTransaction()
        actual()
        trans.commit()
    }
}
