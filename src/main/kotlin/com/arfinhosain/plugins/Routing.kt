package com.arfinhosain.plugins

import com.arfinhosain.authenticate
import com.arfinhosain.data.user.UserDataSource
import com.arfinhosain.getSecretInfo
import com.arfinhosain.hashing.HashingService
import com.arfinhosain.secret.token.TokenConfig
import com.arfinhosain.secret.token.TokenService
import com.arfinhosain.signIn
import com.arfinhosain.signUp
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig,
    userDataSource: UserDataSource
) {
    routing {
        signUp(userDataSource, hashingService)
        authenticate()
        signIn(userDataSource, hashingService, tokenService, tokenConfig)

        getSecretInfo()

    }
}
