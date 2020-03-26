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

package apis.app.v1alpha1

import com.brvith.frameworks.operator.logger
import com.brvith.frameworks.operator.AbstractOperatorApiClient
import com.brvith.frameworks.operator.OperatorApiClient
import com.brvith.frameworks.operator.OperatorConstants
import com.brvith.frameworks.operator.asYaml
import com.brvith.frameworks.operator.logger
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.kubernetes.client.extended.generic.KubernetesApiResponse
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

fun Application.kkkCrdApiModule() {

    val kkkCrdApiHandler by kodein().instance<KKKCrdApiHandler>()

    // Install Custom Resource Definition
    //kkkCrdCApiHandler.installCustomResourceDefinition(kkkCrdBeta1CustomResourceDefinition())

    // Install Custom Resource to YAML Types
    addCrdTypeToYamlModule()
    log.info("Updated YAML Module")

    routing {
        get("/operator/apis/v1alpha1/kkcrds/name/{name}") {
            val name = call.parameters["name"] as String
            val response = kkkCrdApiHandler.getConfig(name)
            call.respond(response)
        }
        get("/operator/apispec/v1alpha1/kkcrds/name/{name}") {
            val name = call.parameters["name"] as String
            val response = kkkCrdApiHandler.getConfigSpec(name)
            call.respond(response)
        }
        post("/operator/apispec/v1alpha1/kkcrds/name/{name}") {
            val name = call.parameters["name"] as String
            val response = kkkCrdApiHandler.setConfigSpec(name, call.receive())
            call.respond(response)
        }
    }
}

/** Rest Handler Class */
open class KKKCrdApiHandler(private val operatorApiClient: OperatorApiClient) {
    private val log = logger(KKKCrdApiHandler::class)
    private val operatorClient = operatorApiClient as AbstractOperatorApiClient
    private val kkkCrdApiClient = operatorClient.kkkCrdApiClient()

    suspend fun getConfigResponse(name: String): KubernetesApiResponse<KKKCrd> {
        val namespace = OperatorConstants.namespace
        return kkkCrdApiClient.get(namespace, name)
    }

    suspend fun getConfig(name: String): KKKCrd {
        return getConfigResponse(name).`object`
    }

    suspend fun getConfigSpec(name: String): KKKCrdSpec {
        return getConfig(name).spec
    }

    suspend fun createConfig(name: String, kkkCrdSpec: KKKCrdSpec): KubernetesApiResponse<KKKCrd> {
        val namespace = OperatorConstants.namespace
        val kkkCrd = KKKCrd()
        kkkCrd.apiVersion = "app.brvith.com/v1alpha1"
        kkkCrd.kind = "KKKCrd"
        kkkCrd.metadata = V1ObjectMetaBuilder()
            .withNamespace(namespace)
            .withName(name)
            .build()
        kkkCrd.spec = kkkCrdSpec
        val createStatus = kkkCrdApiClient.create(kkkCrd)
        log.info("Create Status : ${createStatus.status}")
        return createStatus
    }

    suspend fun setConfigSpec(name: String, kkkCrdSpec: KKKCrdSpec): KKKCrdSpec {
        val getResult = getConfigResponse(name)
        log.info("Get Status : ${getResult.status}")
        if (getResult.httpStatusCode == 404) {
            createConfig(name, kkkCrdSpec)
        } else {
            val kkkCrd = getResult.`object`
            kkkCrd.spec = kkkCrdSpec
            log.info("Updating Config : ${kkkCrd.asYaml()}")
            val updateStatus = kkkCrdApiClient.update(kkkCrd)
            log.info("Update Status : ${updateStatus.status}")
        }
        return getConfigSpec(name)
    }

    suspend fun installCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition) {
        log.info("Installing Custom Resource  : ${customResourceDefinition.asYaml()}")
        val result = operatorApiClient.betaCustomResourceDefinitionsApiClient().create(customResourceDefinition)
        log.info("Installed Custom Resource Result : ${result.status}")
        log.info("Installed Custom Resource  : ${result.`object`.asYaml()} ")
    }
}