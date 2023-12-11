package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
){

    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `유저 저장이 정상 동작 한다`() {
        val request = UserCreateRequest("user1", null)

        userService.saveUser(request)

        val results = userRepository.findAll()
        assertThat(results.size).isEqualTo(1)
        assertThat(results[0].name).isEqualTo("user1")
        assertThat(results[0].age).isNull()
    }

    @Test
    fun `유저 조회가 정상 동작 한다`() {
        userRepository.saveAll(listOf(
            User("user1", 20),
            User("user2", null),
        ))

        val users = userService.getUsers()

        assertThat(users.size).isEqualTo(2)
        assertThat(users).extracting("name").containsExactly("user1", "user2")
        assertThat(users).extracting("age").containsExactly(20, null)
    }

    @Test
    fun `유저 업데이트가 정상 동작 한다`() {
        val savedUser = userRepository.save(User("A", 20))
        val request = UserUpdateRequest(savedUser.id!!, "B")

        userService.updateUserName(request)

        val result = userRepository.findAll()[0]
        assertThat(result.name).isEqualTo("B")
    }

    @Test
    fun `유저 삭제가 정상 동작 한다`() {
        userRepository.save(User("A", 20))
        userService.deleteUser("A")

        val result = userRepository.findAll()
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함된다")
    fun getUserLoanHistoriesTest1() {
        userRepository.save(User("A", null))

        val results = userService.getUserLoanHistories()

        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 많은 유저의 응답이 정상 동작한다")
    fun getUserLoanHistoriesTest2() {
        val saveUser = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
                UserLoanHistory.fixture(saveUser, "책1", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(saveUser, "책2", UserLoanStatus.LOANED),
                UserLoanHistory.fixture(saveUser, "책3", UserLoanStatus.RETURNED),)
        )

        val results = userService.getUserLoanHistories()

        assertThat(results).hasSize(1)
        assertThat(results[0].name).isEqualTo("A")
        assertThat(results[0].books).hasSize(3)
        assertThat(results[0].books).extracting("name").containsExactlyInAnyOrder("책1", "책2", "책3")
        assertThat(results[0].books).extracting("isReturn").containsExactlyInAnyOrder(false, false, true)
    }
}
