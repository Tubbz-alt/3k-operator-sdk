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
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class KKKCrdControllerTest {

    // @Test
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