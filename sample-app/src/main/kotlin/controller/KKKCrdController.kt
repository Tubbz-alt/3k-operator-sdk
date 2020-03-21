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
import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkController
import com.brvith.operatorsdk.core.asYaml
import com.brvith.operatorsdk.core.asYamlObject
import com.brvith.operatorsdk.core.logger
import com.brvith.operatorsdk.core.utils.CallGeneratorUtils
import com.brvith.operatorsdk.core.utils.SharedInformerUtils
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerWatch
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.workqueue.WorkQueue
import io.kubernetes.client.openapi.models.V1alpha1AuditSink
import java.io.File
import java.util.function.Supplier

open class KKKCrdController(
    private val informerFactory: SharedInformerFactory,
    private val operatorApiClient: OperatorSdkApiClient,
    private val operatorSdkController: OperatorSdkController
) {
    private val log = logger(KKKCrdController::class)

    suspend fun registerAudit(file: File) {
        val auditSink = file.asYamlObject<V1alpha1AuditSink>()
        operatorApiClient.auditSinkApiClient().create(auditSink)
    }

    suspend fun startController(namespace: String) {
        log.info("######## Starting CRD Controller ############")

        val apiClient = operatorApiClient.apiClient()

        /** Create Shared index informers */
        val callGenerator = CallGeneratorUtils.customNamedCRDCallGenerator(
            apiClient,
            namespace,
            "app.brvith.com",
            "v1alpha1",
            "kkkcrds"
        )
        val sharedIndexInformer =
            SharedInformerUtils.createSharedInformer<KKKCrd, KKKCrdList>(
                informerFactory,
                callGenerator
            )

        /** Start all the registered Informers */
        informerFactory.startAllRegisteredInformers()

        val nodeReconciler = KKKCrdReconciler(
            operatorApiClient,
            sharedIndexInformer
        )

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

        val readyFunc = Supplier<Boolean> {
            sharedIndexInformer.hasSynced()
        }

        /** Create CRD Controller */
        val controller = operatorSdkController.createController<KKKCrd>(
            "kkkCrd-controller", nodeReconciler, watchBlock,
            readyFunc, 10
        )
        val controllers = arrayListOf<Controller>(controller)

        /** Start the controller */
        operatorSdkController.startController(controllers, "kkkCrd-controller")
    }
}