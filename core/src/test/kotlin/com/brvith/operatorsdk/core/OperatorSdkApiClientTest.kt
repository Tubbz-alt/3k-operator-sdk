package com.brvith.operatorsdk.core

import com.brvith.operatorsdk.core.sample.SampleCRD
import com.brvith.operatorsdk.core.sample.sampleCRDApiClient
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1Role
import io.kubernetes.client.openapi.models.V1RoleBinding
import io.kubernetes.client.openapi.models.V1ServiceAccount
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.util.Config
import io.kubernetes.client.util.Yaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.assertNotNull

class OperatorSdkApiClientTest {

   // @Test
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
            operator.unDeployServiceAccount(namespace, serviceAccount.metadata!!.name!!)
            delay(100)
            operator.deployServiceAccount(namespace, serviceAccountFile)

            // Role
            val roleFile = testResourceFile("ric-operator/role.yaml")
            val role = roleFile.asKubernetesResource<V1Role>()
            operator.unDeployRole(namespace, role.metadata!!.name!!)
            delay(100)
            operator.deployRole(namespace, roleFile)

            // Role Binding
            val roleBindingFile = testResourceFile("ric-operator/role_binding.yaml")
            val roleBinding = roleBindingFile.asKubernetesResource<V1RoleBinding>()
            operator.unDeployRoleBinding(namespace, roleBinding.metadata!!.name!!)
            delay(100)
            operator.deployRoleBinding(namespace, roleBindingFile)

            // Custom Resource Binding
            val crdFile = testResourceFile("ric-operator/crd.yaml")
            val crd = crdFile.asKubernetesResource<V1beta1CustomResourceDefinition>()
            operator.unDeployCustomResourceDefinition(crd.metadata!!.name!!)
            delay(100)
            operator.deployCustomResourceDefinition(crdFile)

            // Operator
            val operatorFile = testResourceFile("ric-operator/operator.yaml")
            val oper = operatorFile.asKubernetesResource<V1Deployment>()
            operator.unDeployDeployment(namespace, oper.metadata!!.name!!)
            delay(100)
            operator.deployDeployment(namespace, oper)
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
