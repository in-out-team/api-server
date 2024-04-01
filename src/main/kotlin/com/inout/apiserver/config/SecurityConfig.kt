package com.inout.apiserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.DefaultSecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig() {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity
    ): DefaultSecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers("/**").permitAll() // TODO: temporarily allow all requests for development
                    // .requestMatchers("/api/**").hasRole("USER") // for future use
                    // .requestMatchers("/admin/**").hasRole("ADMIN") // for future use
            }

        return http.build()
    }
}