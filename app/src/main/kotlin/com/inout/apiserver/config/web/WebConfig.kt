package com.inout.apiserver.config.web

import com.inout.apiserver.domain.user.MongoUserService
import com.inout.apiserver.domain.user.UserService
import com.inout.apiserver.infrastructure.db.user.User
import com.inout.apiserver.infrastructure.mongo.user.MongoUser
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val userService: UserService,
    private val mongoUserService: MongoUserService,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        // TODO: remove this when userService is fully migrated
        resolvers.add(RequestUserArgumentResolver(userService))
        resolvers.add(RequestMongoUserArgumentResolver(mongoUserService))
    }

    class RequestUserArgumentResolver(
        private val userService: UserService,
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(RequestUser::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?,
        ): User? {
            val authentication = SecurityContextHolder.getContext().authentication
            return authentication?.let { userService.getUserByEmail(it.name) }
        }
    }

    class RequestMongoUserArgumentResolver(
        private val userService: MongoUserService,
    ) : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter): Boolean = parameter.hasParameterAnnotation(RequestMongoUser::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?,
        ): MongoUser? {
            val authentication = SecurityContextHolder.getContext().authentication
            return authentication?.let { userService.getUserByEmail(it.name) }
        }
    }
}
