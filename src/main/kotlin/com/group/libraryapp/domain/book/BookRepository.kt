package com.group.libraryapp.domain.book

import org.springframework.data.jpa.repository.JpaRepository

interface BookRepository : JpaRepository<Book, Long>, BookRepositoryCustom {

    fun findByName(bookName: String): Book?
}
