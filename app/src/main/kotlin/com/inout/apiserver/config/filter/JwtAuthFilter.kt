package com.inout.apiserver.config.filter

import com.inout.apiserver.domain.auth.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val userDetailsService: UserDetailsService,
    private val tokenService: TokenService
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String = request.getHeader("Authorization") ?: ""
        // if Authorization header is not present, continue to the next filter
        if (!authHeader.containsBearerPrefix()) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.extractToken()
        val email = tokenService.extractEmail(token)
        /**
         * if email is null, it means the token is invalid (since application signs token with email)
         * if authentication is already set, no need to set it again
         * - this is to prevent overriding the existing authentication
         */
        if (email == null || SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        val user = userDetailsService.loadUserByUsername(email)
        // if token is valid, update security context
        if (tokenService.isValid(token, user.username)) {
            updateSecurityContext(user, request)
        }

        filterChain.doFilter(request, response)
    }

    private fun String.containsBearerPrefix(): Boolean {
        return this.startsWith("Bearer ")
    }

    private fun String.extractToken(): String {
        return this.substringAfter("Bearer ")
    }

    private fun updateSecurityContext(user: UserDetails, request: HttpServletRequest) {
        val authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = authentication
    }
}