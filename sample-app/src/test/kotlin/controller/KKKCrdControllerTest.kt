package controller

import com.brvith.operatorsdk.core.OperatorSdkApiClientImpl
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class KKKCrdControllerTest {

    @Test
    fun testKKKCrdController() {
        runBlocking {
            val namespace = "operator-test"
            val apiClient: ApiClient = Config.defaultClient()
            Configuration.setDefaultApiClient(apiClient)

            val operatorApiClient = OperatorSdkApiClientImpl(apiClient)
            val informerFactory = SharedInformerFactory(apiClient)
            val operatorSdkController = OperatorSdkControllerImpl(informerFactory)

            val controller = KKKCrdController(informerFactory, operatorApiClient, operatorSdkController)
            controller.startController(namespace)
        }
    }
}