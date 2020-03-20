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

import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.asYamlObject
import com.brvith.operatorsdk.core.logger
import com.brvith.operatorsdk.core.testResourceFile
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.models.V1alpha1AuditSink
import io.kubernetes.client.util.Config
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class KKKCrdDataTest {

    private val log = logger(KKKCrdDataTest::class)

    @Test
    fun testCustomResourceDefinition() {
        runBlocking {
            val namespace = "operator-test"
            val client: ApiClient = Config.defaultClient()
            Configuration.setDefaultApiClient(client)
            val operatorSdkApiClient = OperatorSdkApiClientImpl(client)

            // val customResourceDefinitionApi = operatorSdkApiClient.betaCustomResourceDefinitionsApiClient()
            //
            // /** Custom Resource Setup */
            // val crd = kkkCrdBeta1CustomResourceDefinition()
            // log.info("YAML : ${crd.asYaml()}")
            // val deleteStatus = customResourceDefinitionApi.delete(crd.metadata!!.name)
            // println("---- Delete Status :${deleteStatus.status}")
            // val createStatus = customResourceDefinitionApi.create(crd)
            // println("++++ Create Status :${createStatus.status} : ${createStatus.`object`}")

            /** Audit Setup */
            val auditSinkApi = operatorSdkApiClient.auditSinkApiClient()
            val auditSink = testResourceFile("audit_sink.yaml").asYamlObject<V1alpha1AuditSink>()
            val auditDeleteStatus = auditSinkApi.delete(auditSink.metadata!!.name)
            println("--- Delete Status :${auditDeleteStatus.status}")
            val auditCreateResult = auditSinkApi.create(auditSink)
            println("+++ Create Status :${auditCreateResult.status} : ${auditCreateResult.`object`}")

        }
    }
}