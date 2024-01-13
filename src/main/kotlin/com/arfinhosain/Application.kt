package com.arfinhosain

import com.arfinhosain.data.user.MongoUserDataSource
import com.arfinhosain.data.user.User
import com.arfinhosain.plugins.configureMonitoring
import com.arfinhosain.plugins.configureRouting
import com.arfinhosain.plugins.configureSecurity
import com.arfinhosain.plugins.configureSerialization
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    GlobalScope.launch {
        val user = User(
            username = "test",
            password = "dfjksdjfsd",
            salt = "salt"
        )
        userDataSource.insertUser(user)
    }


    configureRouting()
    configureMonitoring()
    configureSerialization()
    configureSecurity()

}
