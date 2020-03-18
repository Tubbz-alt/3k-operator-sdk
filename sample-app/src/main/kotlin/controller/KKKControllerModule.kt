package controller

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
    }
}

/** Rest Handler Class */
open class KKKCrdCApiHandler(private val operatorSdkApiClient: OperatorSdkApiClient) {
    private val log = logger(KKKCrdCApiHandler::class)
    private val operatorClient = operatorSdkApiClient as AbstractOperatorSdkApiClient
    private val kkkCrdApiClient = operatorClient.kkkCrdApiClient()

    suspend fun getConfigure(namespace: String, name: String): KKKCdrSpec {
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }

    suspend fun setConfigure(namespace: String, name: String, kkkCdrSpec: KKKCdrSpec): KKKCdrSpec {
        val getResult = kkkCrdApiClient.get(namespace, name)
        log.info("Get Status : ${getResult.status}")
        if (getResult.httpStatusCode == 404) {
            val kkkCrd = KKKCdr()
            kkkCrd.apiVersion = "app.brvith.com/v1alpha1"
            kkkCrd.kind = "KKKCrd"
            kkkCrd.metadata = V1ObjectMetaBuilder()
                .withNamespace(namespace)
                .withName(name)
                .build()
            kkkCrd.spec = kkkCdrSpec
            log.info("Creating Config : ${kkkCrd.asYaml()}")
            val createStatus = kkkCrdApiClient.create(kkkCrd)
            log.info("Create Status : ${createStatus.status}")
        } else {
            val kkkCrd = getResult.`object`
            kkkCrd.spec = kkkCdrSpec
            log.info("Updating Config : ${kkkCrd.asYaml()}")
            val updateStatus = kkkCrdApiClient.update(kkkCrd)
            log.info("Update Status : ${updateStatus.status}")
        }
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }

    suspend fun installCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition) {
        log.info("Installing Custom Resource  : ${customResourceDefinition.asYaml()}")
        val result = operatorSdkApiClient.beta1customResourceDefinitionsApiClient().create(customResourceDefinition)
        log.info("Installed Custom Resource Result : ${result.status}")
        log.info("Installed Custom Resource  : ${result.`object`.asYaml()} ")
    }
}