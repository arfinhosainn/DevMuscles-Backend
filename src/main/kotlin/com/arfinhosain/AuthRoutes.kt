package com.arfinhosain

import com.arfinhosain.data.user.User
import com.arfinhosain.data.user.UserDataSource
import com.arfinhosain.hashing.HashingService
import com.arfinhosain.hashing.SaltedHash
import com.arfinhosain.requests.AuthRequests
import com.arfinhosain.responses.AuthResponse
import com.arfinhosain.secret.ApiKeys
import com.arfinhosain.secret.token.TokenClaim
import com.arfinhosain.secret.token.TokenConfig
import com.arfinhosain.secret.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.codec.digest.DigestUtils

fun Route.signUp(
    userDataSource: UserDataSource,
    hashingService: HashingService
) {
    post("signup") {
        val request = call.receiveOrNull<AuthRequests>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val areFieldBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 8
        if (areFieldBlank || isPwTooShort) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val apiKey = call.request.headers["X-API-Key"]
        if (apiKey == null || apiKey != ApiKeys.API_KEY) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid API key")
            return@post
        }


        val saltedHash = hashingService.generatedSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }
        call.respond(HttpStatusCode.OK, "Signup was successful")
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = call.receiveOrNull<AuthRequests>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if(user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword) {
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }


        val apiKey = call.request.headers["X-API-Key"]
        if (apiKey == null || apiKey != ApiKeys.API_KEY) {
            call.respond(HttpStatusCode.Unauthorized, "Invalid API key")
            return@post
        }


        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}
fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your user id is $userId")
        }
    }
}

