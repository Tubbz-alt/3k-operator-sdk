/*
 * Copyright © 2020 Brvith Solutions.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package controller

import apis.app.v1alpha1.addCrdTypeToYamlModule
import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.OperatorSdkController
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
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

    bind<KKKCrdApiHandler>() with singleton { KKKCrdApiHandler(instance()) }
}

/** Application Setup */
fun Application.featureKKKCrdController() {
    log.info("Installing KKKCrd Controller")
    val kkkCrdCApiHandler by kodein().instance<KKKCrdApiHandler>()

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
