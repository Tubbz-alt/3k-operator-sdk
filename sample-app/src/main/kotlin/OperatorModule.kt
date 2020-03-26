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

import apis.app.v1alpha1.KKKCrdApiHandler
import apis.app.v1alpha1.kkkCrdApiModule
import com.brvith.frameworks.operator.OperatorApiClient
import com.brvith.frameworks.operator.OperatorApiClientImpl
import com.brvith.frameworks.operator.OperatorControllerManager
import com.brvith.frameworks.operator.OperatorControllerManagerImpl
import controller.KKKCrdController
import controller.controllerManagerModule
import controlloop.ControlLoopHandler
import controlloop.controlLoopModule
import io.ktor.application.Application
import io.ktor.application.log
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.kodein.di.ktor.kodein

/** Kodein dependency Management */
val operatorTestKodein = Kodein.Module("operatorTestKodein") {

    bind<ApiClient>() with singleton { Config.defaultClient() }

    bind<SharedInformerFactory>() with singleton {
        val apiClient: ApiClient = instance()
        Configuration.setDefaultApiClient(apiClient)

        val sharedInformerFactory = SharedInformerFactory(apiClient)
        sharedInformerFactory
    }

    bind<OperatorApiClient>() with singleton {
        OperatorApiClientImpl(
            instance()
        )
    }
    bind<OperatorControllerManager>() with singleton {
        OperatorControllerManagerImpl(
            instance()
        )
    }

    /** Apis */
    bind<KKKCrdApiHandler>() with singleton { KKKCrdApiHandler(instance()) }
    /** Controller */
    bind<KKKCrdController>() with singleton { KKKCrdController(instance(), instance()) }
    /** Control Loop */
    bind<ControlLoopHandler>() with singleton { ControlLoopHandler(instance()) }

}

/** Application Setup */
fun Application.operatorTestModule() {

    kodein { importAll(operatorTestKodein) }

    log.info("Installing Operator Test")
    /** Register API Module */
    kkkCrdApiModule()
    /** Register Controller Manager Module */
    controllerManagerModule()
    /** Register API Module */
    controlLoopModule()
}
