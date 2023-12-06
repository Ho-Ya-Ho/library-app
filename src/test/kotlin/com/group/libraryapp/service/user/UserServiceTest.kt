package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userService: UserService,
    private val userRepository: UserRepository
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
}
