package controller

import apis.app.v1alpha1.KKKCdr
import apis.app.v1alpha1.KKKCdrSpec
import apis.app.v1alpha1.addCrdTypeToYamlModule
import apis.app.v1alpha1.kkkCrdApiClient
import com.brvith.operatorsdk.core.AbstractOperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.OperatorSdkController
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
import com.brvith.operatorsdk.core.asYaml
import com.brvith.operatorsdk.core.logger
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.util.Config
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

/** Kodein dependency Management */
val featureKKKCrdController = Kodein.Module("featureKKKCrdController") {
    bind<ApiClient>() with singleton { Config.defaultClient() }

    bind<SharedInformerFactory>() with singleton {
        val apiClient: ApiClient = instance()
        Configuration.setDefaultApiClient(apiClient)

        val sharedInformerFactory = SharedInformerFactory(apiClient)
        sharedInformerFactory
    }

    bind<OperatorSdkApiClient>() with singleton { OperatorSdkApiClientImpl(instance()) }
    bind<OperatorSdkController>() with singleton { OperatorSdkControllerImpl(instance()) }

    bind<KKKCrdController>() with singleton { KKKCrdController(instance(), instance(), instance()) }

    bind<KKKCrdCApiHandler>() with singleton { KKKCrdCApiHandler(instance()) }
}

/** Application Setup */
fun Application.featureKKKCrdController() {
    log.info("Installing KKKCrd Controller")
    val kkkCrdCApiHandler by kodein().instance<KKKCrdCApiHandler>()

    val kkkCrdController by kodein().instance<KKKCrdController>()

    /** Run the Controller in separate Thread */
    val deferred = async(Dispatchers.IO) {

        // Install Custom Resource Definition
        //kkkCrdCApiHandler.installCustomResourceDefinition(kkkCrdBeta1CustomResourceDefinition())

        // Install Custom Resource to YAML Types
        addCrdTypeToYamlModule()
        log.info("Updated YAML Module")

        kkkCrdController.startController("operator-test")

    }
    deferred.start()

    log.info("Starting KKKCrd Controller API...")

    routing {
        get("/controller/ping") {
            call.respondText("KKKCrd Controller Success")
        }
        get("/controller/config/namespace/{namespace}/name/{name}") {
            val namespace = call.parameters["namespace"] as String
            val name = call.parameters["name"] as String
            val response = kkkCrdCApiHandler.getConfigure(namespace, name)
            call.respond(response)
        }
        post("/controller/config/namespace/{namespace}/name/{name}") {
            val namespace = call.parameters["namespace"] as String
            val name = call.parameters["name"] as String
            val response = kkkCrdCApiHandler.setConfigure(namespace, name, call.receive())
            call.respond(response)
        }
        post("/controller/auditsink") {
            //log.info("--------------> Received Audit Event ")
            val request = call.receive<Any>()
            log.info("--------------> Received Audit Event : ${request}")
            call.respond(HttpStatusCode.OK)
        }
    }
}
