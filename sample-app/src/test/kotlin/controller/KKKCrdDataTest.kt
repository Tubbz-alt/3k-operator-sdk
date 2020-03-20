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