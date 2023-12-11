package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import com.group.libraryapp.dto.book.response.BookStatResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
) {

    @AfterEach
    fun clean() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `책 저장이 정상 동작 한다`() {
        val request = BookRequest("book1", BookType.COMPUTER)

        bookService.saveBook(request)

        val results = bookRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("book1")
        assertThat(results[0].type).isEqualTo(BookType.COMPUTER)
    }

    @Test
    fun `책 대출이 정상 동작 한다`() {
        bookRepository.save(Book.fixture("book1"))
        val savedUser = userRepository.save(User("user1", 20))
        val request = BookLoanRequest("user1", "book1")

        bookService.loanBook(request)

        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].bookName).isEqualTo("book1")
        assertThat(results[0].user.id).isEqualTo(savedUser.id)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.LOANED)
    }

    @Test
    fun `책이 진짜 대출되어 있다면, 신규 대출이 실패한다`() {
        bookRepository.save(Book.fixture("책 이름"))
        val savedUser = userRepository.save(User("user1", 20))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser))
        val request = BookLoanRequest("user1", "책 이름")

        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message

        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")
    }

    @Test
    fun `책 반납이 정상 동작한다`() {
        val savedUser = userRepository.save(User("user1", 20))
        userLoanHistoryRepository.save(UserLoanHistory.fixture(savedUser))
        val request = BookReturnRequest("user1", "책 이름")

        bookService.returnBook(request)

        val results = userLoanHistoryRepository.findAll()
        assertThat(results).hasSize(1)
        assertThat(results[0].status).isEqualTo(UserLoanStatus.RETURNED)
    }

    @Test
    fun `책 대여 권수를 정상 확인한다`() {
        val savedUser = userRepository.save(User("user1", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(savedUser, "A"),
            UserLoanHistory.fixture(savedUser, "B", UserLoanStatus.RETURNED),
            UserLoanHistory.fixture(savedUser, "C", UserLoanStatus.RETURNED)
        ))

        val result = bookService.countLoanedBook()

        assertThat(result).isEqualTo(1)
    }

    @Test
    @DisplayName("분야별 권수를 정상 확인한다")
    fun getBookStatisticsTest() {
        bookRepository.saveAll(listOf(
            Book.fixture("A", BookType.COMPUTER),
            Book.fixture("B", BookType.COMPUTER),
            Book.fixture("C", BookType.SCIENCE)
        ))

        val results = bookService.getBookStatistics()

        assertThat(results).hasSize(2)
        assertCount(results, BookType.COMPUTER, 2L)
        assertCount(results, BookType.SCIENCE, 1L)

    }

    private fun assertCount(results: List<BookStatResponse>, type: BookType, count: Long) {
        assertThat(results.first { it.type == type }.count).isEqualTo(count)
    }
}
