package com.inout.apiserver.config.security

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.inout.apiserver.config.filter.JwtAuthFilter
import com.inout.apiserver.infrastructure.db.user.UserRepository
import com.inout.apiserver.infrastructure.security.CustomUserDetailsService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userRepository: UserRepository,
) {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
    ): DefaultSecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .permitAll() // permit all swagger related requests (TODO: this is for dev, disable on prod)
                    .requestMatchers(HttpMethod.POST, "/v1/users")
                    .permitAll() // for user creation, no authentication required
                    .requestMatchers("/v*/auth/**")
                    .permitAll() // for user authentication, no authentication required
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN") // admin role check
                    .requestMatchers("/**")
                    .hasRole("USER") // user role check
            }.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // since we are using JWT
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.contentType = "application/json;charset=UTF-8"
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    // FIXME: unwanted error responses are also sent as unauthorized, need to fix this
                    response.writer.write(
                        """
                        {
                            "message": "Unauthorized: Invalid Token or No Token Provided",
                            "code": "UNAUTHORIZED_1",
                            "extraData": {}
                        }
                        """.trimIndent(),
                    )
                }
            }

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService = CustomUserDetailsService(userRepository)

    @Bean
    fun encoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): AuthenticationProvider =
        DaoAuthenticationProvider()
            .apply {
                setUserDetailsService(userDetailsService())
                setPasswordEncoder(encoder())
            }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager

    @Bean // place oauth related here for now, on implementing other providers(ex. apple, kakao), relocate to a different config
    fun googleIdTokenVerifier(
        @Value("\${auth.google.ios-client-id}")
        googleIosClientId: String,
    ): GoogleIdTokenVerifier =
        GoogleIdTokenVerifier
            .Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf(googleIosClientId))
            .build()
}
