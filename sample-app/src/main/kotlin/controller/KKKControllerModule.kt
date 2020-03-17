package controller

import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.OperatorSdkController
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

val featureKKKCrdController = Kodein.Module("featureKKKCrdController") {
    bind<ApiClient>() with singleton { Config.defaultClient() }

    bind<OperatorSdkApiClient>() with singleton { OperatorSdkApiClientImpl(instance()) }
    bind<OperatorSdkController>() with singleton { OperatorSdkControllerImpl(instance()) }

    bind<KKKCrdController>() with singleton {
        KKKCrdController(
            instance()
        )
    }

    bind<KKKCrdCApiHandler>() with singleton {
        KKKCrdCApiHandler(
            instance()
        )
    }

}

fun Application.featureKKKCrdController() {
    log.info("Installing KKKCrd Controller")
    val kkkCrdCApiHandler by kodein().instance<KKKCrdCApiHandler>()

    val kkkCrdController by kodein().instance<KKKCrdController>()

    runBlocking {
        launch(Dispatchers.IO) {
            // Install Custom Resource to YAML Types
            addCrdToYamlModule()

            kkkCrdController.startController("operator-test")
        }
    }

    routing {
        get("/controller/ping") {
            call.respondText("KKKCrd Controller Success")
        }
        get("/controller/configure/namespace/{namespace}/name/name/{name}") {
            val namespace = call.parameters["namespace"] as String
            val name = call.parameters["name"] as String
            val response = kkkCrdCApiHandler.getConfigure(namespace, name)
            call.respond(response)
        }
        post("/controller/configure/{namespace}/name/name/{name}") {
            val namespace = call.parameters["namespace"] as String
            val name = call.parameters["name"] as String
            val response = kkkCrdCApiHandler.setConfigure(namespace, name, call.receive())
            call.respond(response)
        }
    }
}

class KKKCrdCApiHandler(private val operatorSdkApiClient: OperatorSdkApiClient) {
    private val kkkCrdApiClient =
        kkkCrdApiClient(operatorSdkApiClient.apiClient())

    suspend fun getConfigure(namespace: String, name: String): KKKCdrSpec {
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }

    suspend fun setConfigure(namespace: String, name: String, kkkCdrSpec: KKKCdrSpec): KKKCdrSpec {
        val kkkCrd = kkkCrdApiClient.get(namespace, name).`object`
        kkkCrd.spec = kkkCdrSpec
        kkkCrdApiClient.update(kkkCrd)
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }
}