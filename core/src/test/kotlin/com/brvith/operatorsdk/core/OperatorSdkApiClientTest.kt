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

package com.brvith.operatorsdk.core

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1Role
import io.kubernetes.client.openapi.models.V1RoleBinding
import io.kubernetes.client.openapi.models.V1ServiceAccount
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.util.Config
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class OperatorSdkApiClientTest {

    @Test
    fun testOperatorDeployment() {
        runBlocking {
            val namespace = "operator-test"
            val client: ApiClient = Config.defaultClient()
            Configuration.setDefaultApiClient(client)
            val operator = OperatorSdkApiClientImpl(client)

            operator.createNamespace(namespace)

            // Service Account
            val serviceAccountFile = testResourceFile("ric-operator/service_account.yaml")
            val serviceAccount = serviceAccountFile.asKubernetesResource<V1ServiceAccount>()
            operator.deleteServiceAccount(namespace, serviceAccount.metadata!!.name!!)
            delay(100)
            operator.applyServiceAccount(namespace, serviceAccountFile)

            // Role
            val roleFile = testResourceFile("ric-operator/role.yaml")
            val role = roleFile.asKubernetesResource<V1Role>()
            operator.deleteRole(namespace, role.metadata!!.name!!)
            delay(100)
            operator.applyRole(namespace, roleFile)

            // Role Binding
            val roleBindingFile = testResourceFile("ric-operator/role_binding.yaml")
            val roleBinding = roleBindingFile.asKubernetesResource<V1RoleBinding>()
            operator.deleteRoleBinding(namespace, roleBinding.metadata!!.name!!)
            delay(100)
            operator.applyRoleBinding(namespace, roleBindingFile)

            // Custom Resource Binding
            val crdFile = testResourceFile("ric-operator/crd.yaml")
            val crd = crdFile.asKubernetesResource<V1beta1CustomResourceDefinition>()
            operator.deleteCustomResourceDefinition(crd.metadata!!.name!!)
            delay(100)
            operator.applyCustomResourceDefinition(crdFile)

            // Operator
            val operatorFile = testResourceFile("ric-operator/operator.yaml")
            val oper = operatorFile.asKubernetesResource<V1Deployment>()
            operator.deleteDeployment(namespace, oper.metadata!!.name!!)
            delay(100)
            operator.createDeployment(namespace, oper)
            /*

            // Custom Resource
            Yaml.addModelMap("app.brvith.com/v1alpha1", "SelfService", SampleCRD::class.java)
            val crFile = testResourceFile("ric-operator/cr.yaml")
            val cr = Yaml.load(crFile) as SampleCRD
            assertNotNull(cr)
            cr.metadata.namespace = namespace
            val crdClient = sampleCRDApiClient(client)
            val deployedCR = crdClient.create(cr)
            assertNotNull(deployedCR)

            //delay(10000)

            //operator.deleteNamespace(namespace)
            */

        }
    }
}
