package com.group.libraryapp.domain.book

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id

@Entity
class Book(
    val name: String,

    val type: String,

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,
) {
    init {
        require(name.isNotBlank()) { "이름은 비어 있을 수 없습니다" }
    }

    companion object {
        fun createBookFixture(
            name: String ="책 이름",
            type: String = "COMPUTER",
            id: Long? = null,
        ): Book {
            return Book(
                name = name,
                type = type,
                id = id,
            )
        }
    }
}
