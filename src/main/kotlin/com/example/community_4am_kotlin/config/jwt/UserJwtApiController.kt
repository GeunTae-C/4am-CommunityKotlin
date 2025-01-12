package com.example.community_4am_kotlin.config.jwt

import com.example.community_4am_kotlin.domain.user.Role
import com.example.community_4am_kotlin.domain.user.User
import com.example.community_4am_kotlin.domain.user.RefreshToken
import com.example.community_4am_kotlin.feature.user.util.CookieUtil
import com.example.community_4am_kotlin.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository
import com.example.community_4am_kotlin.feature.user.repository.RefreshTokenRepository
import com.example.community_4am_kotlin.feature.user.service.UserDetailService
import com.example.community_4am_kotlin.feature.user.service.UserService
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.hibernate.query.sqm.tree.SqmNode.log
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.time.Duration

@RestController

class UserJwtApiController(
    private val userService: UserService,
    private val userDetailService: UserDetailService,
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val authorizationRequestRepository: OAuth2AuthorizationRequestBasedOnCookieRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    companion object {
        const val REFRESH_TOKEN_COOKIE_NAME = "refresh_token"
        val REFRESH_TOKEN_DURATION = Duration.ofDays(14)
        val ACCESS_TOKEN_DURATION = Duration.ofDays(1)
        const val REDIRECT_PATH = "/articles"
    }

    @PostMapping("/api/login")
    @Throws(ServletException::class, IOException::class)
    fun login(@RequestBody loginRequest: LoginRequest, request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<User> {
        val user = userDetailService.loadUserByUsername(loginRequest.username) as? User
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        if (BCryptPasswordEncoder().matches(loginRequest.password, user.password)) {
            val refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION)
            user.id?.let { saveRefreshToken(it, refreshToken, user.email) }
            addRefreshTokenToCookie(request, response, refreshToken)

            val accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION)
            val targetUrl = getTargetUrl(accessToken)
            clearAuthenticationAttributes(request, response)
            response.setHeader("Authorization", "Bearer $accessToken")

            val authenticationToken = UsernamePasswordAuthenticationToken(
                JwtPrincipal(user.username),
                null,
                listOf(SimpleGrantedAuthority(Role.ROLE_USER.getAuthority()))
            )
            SecurityContextHolder.getContext().authentication = authenticationToken

            return ResponseEntity.status(HttpStatus.OK).body(user)
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    private fun saveRefreshToken(userId: Long, newRefreshToken: String, email: String) {
        val refreshToken = refreshTokenRepository.findByUserId(userId)
            .map { it.update(newRefreshToken) }
            .orElseGet { RefreshToken(userId = userId, refreshToken = newRefreshToken, email = email) }

        refreshTokenRepository.save(refreshToken)
    }

    private fun addRefreshTokenToCookie(request: HttpServletRequest, response: HttpServletResponse, refreshToken: String) {
        val cookieMaxAge = REFRESH_TOKEN_DURATION.seconds.toInt()
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME)
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge)
    }

    private fun getTargetUrl(token: String): String {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
            .queryParam("token", token)
            .build()
            .toUriString()
    }

    private fun clearAuthenticationAttributes(request: HttpServletRequest, response: HttpServletResponse) {
        super.clearAuthenticationAttributes(request)
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
    }
}