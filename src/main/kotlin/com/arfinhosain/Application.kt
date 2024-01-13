package com.arfinhosain

import com.arfinhosain.data.user.MongoUserDataSource
import com.arfinhosain.hashing.SHA256HashingService
import com.arfinhosain.plugins.configureMonitoring
import com.arfinhosain.plugins.configureRouting
import com.arfinhosain.plugins.configureSecurity
import com.arfinhosain.plugins.configureSerialization
import com.arfinhosain.secret.token.JwtTokenService
import com.arfinhosain.secret.token.TokenConfig
import io.ktor.server.application.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@OptIn(DelicateCoroutinesApi::class)
fun Application.module() {
    val mongoPw = System.getenv("MONGO_PW")
    val dbName = "devmuscles"
    val db = KMongo.createClient(
        "mongodb+srv://$dbName:$mongoPw@cluster0.49rueui.mongodb.net/?retryWrites=true&w=majority"
    ).coroutine
        .getDatabase(dbName)

    val userDataSource = MongoUserDataSource(db)

    val tokenService = JwtTokenService()
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )

    val hashingService = SHA256HashingService()

    configureSecurity(tokenConfig)
    configureRouting(hashingService, tokenService, tokenConfig, userDataSource)
    configureSerialization()
    configureMonitoring()


}
