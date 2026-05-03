package com.cao.repairshop.user.entity

import com.cao.repairshop.user.domain.UserRole
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "tb_user")
class User(
    @Id
    @Column(name = "id_tb_user")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, length = 150)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "function", nullable = false, length = 50)
    var function: UserRole,

    @Column(nullable = false, unique = true, length = 255)
    var email: String,

    @Column(length = 20)
    var phone: String? = null,

    @Column(nullable = false, length = 255)
    var password: String
) {
    override fun equals(other: Any?): Boolean = other is User && id == other.id
    override fun hashCode(): Int = id.hashCode()
    override fun toString(): String = "User(id=$id, email=$email)"
}
