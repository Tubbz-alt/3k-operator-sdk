package controller

import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.asYaml
import com.brvith.operatorsdk.core.logger
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
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

            val customResourceDefinitioApi = operatorSdkApiClient.beta1customResourceDefinitionsApiClient()

            val crd = kkkCrdBeta1CustomResourceDefinition()
            log.info("YAML : ${crd.asYaml()}")
            val deleteStatus = customResourceDefinitioApi.delete(crd.metadata!!.name)
            println("Delete Status :${deleteStatus.status}")
            val createStatus = customResourceDefinitioApi.create(crd)
            println("Create Status :${createStatus.status} : ${createStatus.`object`}")

        }
    }
}