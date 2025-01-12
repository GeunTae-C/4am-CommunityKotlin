package com.example.community_4am_kotlin.feature.user.service

import com.example.community_4am_kotlin.feature.user.dto.AddUserRequest
import com.example.community_4am_kotlin.domain.user.Role
import com.example.community_4am_kotlin.domain.user.User
import com.example.community_4am_kotlin.domain.user.enums.UserStatus
import com.example.community_4am_kotlin.feature.article.repository.ArticleRepository
import com.example.community_4am_kotlin.feature.comment.repository.CommentRepository
import com.example.community_4am_kotlin.feature.user.repository.UserRepository
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDateTime
import java.util.UUID

@Service

class UserService(
    private val userRepository: UserRepository, // 사용자 정보를 처리하는 레포지토리
    private val articleRepository: ArticleRepository,
    private val commentRepository: CommentRepository
) {

    // 사용자 저장 메서드 (회원가입)
    fun save(dto: AddUserRequest): Long {
        val encoder = BCryptPasswordEncoder()
        var profileImageBytes: ByteArray? = null
        var profileUrl: String? = null

        if (!dto.profileImage.isEmpty) {
            try {
                profileImageBytes = dto.profileImage.bytes
                val fileName = "${UUID.randomUUID()}_${dto.profileImage.originalFilename}"
                profileUrl = fileName
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Failed to process the profile image", e)
            }
        } else {
            try {
                val defaultImage = File("src/main/resources/static/img/default.jpeg")
                profileImageBytes = Files.readAllBytes(defaultImage.toPath())
                val fileName = "${UUID.randomUUID()}_default.jpeg"
                profileUrl = fileName
            } catch (e: IOException) {
                e.printStackTrace()
                throw RuntimeException("Failed to load the default profile image", e)
            }
        }

        // User 클래스가 data class로 선언되어 있어야 합니다
        val user = User(
            email = dto.email,
            password = encoder.encode(dto.password),
            nickname = dto.nickname,
            profileImage = profileImageBytes,
            profileUrl = profileUrl,
            role = Role.ROLE_USER,
            lastActiveTime = LocalDateTime.now()

        )

        return userRepository.save(user).id!!
    }

    // ID로 사용자 조회
    fun findById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("Unexpected user") }
    }

    // 이메일로 사용자 조회
    fun findByEmail(email: String): User {
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("No user found with email: $email") }
    }

    // 사용자 탈퇴
    @Transactional
    fun deleteUserByUsername(username: String) {
        val user = userRepository.findByEmail(username)
            .orElseThrow { IllegalArgumentException("No user found with email: $username") }

        // 사용자 이메일로 작성된 게시글과 댓글의 작성자 필드를 "탈퇴한 사용자입니다."로 변경
//        articleRepository.updateAuthorToDeleted(username)
//        commentRepository.updateCommentAuthorToDeleted(username)
        userRepository.delete(user)
    }

    // 특정 사용자의 닉네임을 null로 설정하는 메서드 (이메일로 사용자 조회)
    @Transactional // 데이터 변경 시 트랜잭션을 보장함
    fun setNicknameNullByEmail(email: String) {
        log.info("닉네임을 null로 변경할 이메일: $email")
        userRepository.findByEmail(email).ifPresent { user ->
            // 닉네임을 null로 변경 후 저장
            user.update(null)
            userRepository.save(user)
            log.info("닉네임이 null로 변경됨: $user")
        }
    }

    // 사용자가 좋아요 누른 게시글 조회
    // 사용자가 쓴 게시글 조회
    // 사용자가 쓴 댓글 조회

    fun findByUsername(username: String): User {
        val user = userRepository.findByEmail(username)
        return user.orElseThrow { IllegalArgumentException("No user found with email: $username") }
    }

    fun searchUsersByEmailStartingWith(email: String): List<User> {
        return userRepository.findByEmailStartingWith(email)
    }
    @Async
    @Transactional
    fun updateLastActiveTime(userId: String?) {
        val user = userId?.let { userRepository.findByEmail(it).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") } }
        user?.lastActiveTime = LocalDateTime.now()
        user?.status = UserStatus.ONLINE // 활동 시 상태를 ONLINE으로 업데이트
        user?.let { userRepository.save(it) }
    }

    fun findUserIdByEmail(email: String): Long? {
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("해당 이메일을 가진 사용자를 찾을 수 없습니다.") }
        return user.id
    }
}


