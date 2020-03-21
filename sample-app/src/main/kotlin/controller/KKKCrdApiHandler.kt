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

import apis.app.v1alpha1.KKKCrd
import apis.app.v1alpha1.KKKCrdSpec
import apis.app.v1alpha1.kkkCrdApiClient
import com.brvith.operatorsdk.core.AbstractOperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.asYaml
import com.brvith.operatorsdk.core.logger
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition

/** Rest Handler Class */
open class KKKCrdApiHandler(private val operatorSdkApiClient: OperatorSdkApiClient) {
    private val log = logger(KKKCrdApiHandler::class)
    private val operatorClient = operatorSdkApiClient as AbstractOperatorSdkApiClient
    private val kkkCrdApiClient = operatorClient.kkkCrdApiClient()

    suspend fun getConfigure(namespace: String, name: String): KKKCrdSpec {
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }

    suspend fun setConfigure(namespace: String, name: String, kkkCrdSpec: KKKCrdSpec): KKKCrdSpec {
        val getResult = kkkCrdApiClient.get(namespace, name)
        log.info("Get Status : ${getResult.status}")
        if (getResult.httpStatusCode == 404) {
            val kkkCrd = KKKCrd()
            kkkCrd.apiVersion = "app.brvith.com/v1alpha1"
            kkkCrd.kind = "KKKCrd"
            kkkCrd.metadata = V1ObjectMetaBuilder()
                .withNamespace(namespace)
                .withName(name)
                .build()
            kkkCrd.spec = kkkCrdSpec
            log.info("Creating Config : ${kkkCrd.asYaml()}")
            val createStatus = kkkCrdApiClient.create(kkkCrd)
            log.info("Create Status : ${createStatus.status}")
        } else {
            val kkkCrd = getResult.`object`
            kkkCrd.spec = kkkCrdSpec
            log.info("Updating Config : ${kkkCrd.asYaml()}")
            val updateStatus = kkkCrdApiClient.update(kkkCrd)
            log.info("Update Status : ${updateStatus.status}")
        }
        return kkkCrdApiClient.get(namespace, name).`object`.spec
    }

    suspend fun installCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition) {
        log.info("Installing Custom Resource  : ${customResourceDefinition.asYaml()}")
        val result = operatorSdkApiClient.betaCustomResourceDefinitionsApiClient().create(customResourceDefinition)
        log.info("Installed Custom Resource Result : ${result.status}")
        log.info("Installed Custom Resource  : ${result.`object`.asYaml()} ")
    }
}