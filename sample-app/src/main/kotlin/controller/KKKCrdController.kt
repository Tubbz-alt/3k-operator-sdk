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
import apis.app.v1alpha1.KKKCrdList
import com.brvith.frameworks.operator.logger
import com.brvith.frameworks.operator.OperatorApiClient
import com.brvith.frameworks.operator.OperatorConstants
import com.brvith.frameworks.operator.OperatorControllerManager
import com.brvith.frameworks.operator.asYaml
import com.brvith.frameworks.operator.logger
import com.brvith.frameworks.operator.utils.CallGeneratorUtils
import com.brvith.frameworks.operator.utils.SharedInformerUtils
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.extended.controller.ControllerWatch
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.workqueue.WorkQueue
import java.util.function.Supplier

open class KKKCrdController(
    private val informerFactory: SharedInformerFactory,
    private val operatorApiClient: OperatorApiClient
) {
    private val log = logger(KKKCrdController::class)

    suspend fun createController(operatorControllerManager: OperatorControllerManager) {
        log.info("######## Creating Controller ############")

        val apiClient = operatorApiClient.apiClient()

        /** Create Shared index informers */
        val callGenerator = CallGeneratorUtils.customNamedCRDCallGenerator(
            apiClient,
            OperatorConstants.namespace,
            "app.brvith.com",
            "v1alpha1",
            "kkkcrds"
        )
        val sharedIndexInformer =
            SharedInformerUtils.createSharedInformer<KKKCrd, KKKCrdList>(informerFactory, callGenerator)

        /** Start all the registered Informers */
        informerFactory.startAllRegisteredInformers()

        val nodeReconciler = KKKCrdReconciler(operatorApiClient, sharedIndexInformer)

        val watchBlock = fun(workQueue: WorkQueue<Request>): ControllerWatch<KKKCrd> {
            return ControllerBuilder.controllerWatchBuilder(KKKCrd::class.java, workQueue)
                // .withWorkQueueKeyFunc { node: KKKCdr ->
                //     val key = "${node.metadata.namespace}/${node.metadata.name}"
                //     Request(key)
                // }
                .withOnAddFilter { createdNode: KKKCrd ->
                    log.debug("Watch Adding CRD :${createdNode.kind},${createdNode.metadata.name}")
                    true
                }
                .withOnUpdateFilter { oldNode: KKKCrd, newNode: KKKCrd ->
                    log.info("Watch Updated CRD : ${newNode.asYaml()}")
                    oldNode.metadata.resourceVersion != newNode.metadata.resourceVersion
                }
                .withOnDeleteFilter { deletedNode: KKKCrd, stateUnknown: Boolean ->
                    log.info("Watch Deleting CRD : ${deletedNode.kind}, ${deletedNode.metadata.name}")
                    true
                }
                .build()
        }

        val readyFunc = Supplier<Boolean> { sharedIndexInformer.hasSynced() }

        /** Create CRD Controller */
        val controller = operatorControllerManager.createController<KKKCrd>(
            "kkkCrd-controller", nodeReconciler, watchBlock,
            readyFunc, 10
        )

        /** Register the controller */
        operatorControllerManager.registerController(controller)
    }
}