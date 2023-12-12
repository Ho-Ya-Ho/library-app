package com.group.libraryapp.domain.book

import com.group.libraryapp.domain.book.QBook.book
import com.group.libraryapp.dto.book.response.BookStatResponse
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory

class BookRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory,
) : BookRepositoryCustom {

    override fun getStats(): List<BookStatResponse> {
        return queryFactory
            .select(Projections.constructor(
                BookStatResponse::class.java,
                book.type,
                book.count()
                )
            )
            .from(book)
            .groupBy(book.type)
            .fetch()
    }
}
