package com.group.libraryapp.domain.book

import com.group.libraryapp.dto.book.response.BookStatResponse

interface BookRepositoryCustom {

    fun getStats(): List<BookStatResponse>
}
