package com.inout.apiserver.config.filter

import com.inout.apiserver.error.InvalidCredentialsException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtExceptionHandlerFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: InvalidCredentialsException) {
            response.contentType = "application/json;charset=UTF-8"
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write(
                """
                {
                    "message": "${e.message}",
                    "code": "${e.code}",
                    "extraData": {}
                }
                """.trimIndent(),
            )
        }
    }
}
