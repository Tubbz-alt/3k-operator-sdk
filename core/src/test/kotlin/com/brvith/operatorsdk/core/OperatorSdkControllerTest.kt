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

import com.brvith.operatorsdk.core.sample.SampleCRD
import com.brvith.operatorsdk.core.sample.SampleCRDList
import com.brvith.operatorsdk.core.sample.SampleCRDReconciler
import com.brvith.operatorsdk.core.sample.sampleCRDApiClient
import com.brvith.operatorsdk.core.utils.CallGeneratorUtils
import com.brvith.operatorsdk.core.utils.SharedInformerUtils
import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerWatch
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.workqueue.WorkQueue
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.Config
import io.kubernetes.client.util.Yaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import java.util.function.Supplier

class OperatorSdkControllerTest {
    private val log = logger(OperatorSdkControllerTest::class)

    //@Test
    fun testOperatorControllerForCRD() {
        runBlocking {
            log.info("######## Testing CRD Controller ############")
            val namespace = "operator-test"

            Yaml.addModelMap("app.brvith.com/v1alpha1", "SelfService", SampleCRD::class.java)
            val crFile = testResourceFile("ric-operator/cr.yaml")
            val cr = Yaml.load(crFile) as SampleCRD
            cr.metadata.namespace = namespace

            val apiClient: ApiClient = Config.defaultClient()
            Configuration.setDefaultApiClient(apiClient)

            val crdClient = sampleCRDApiClient(apiClient)

            val operatorApiClient = OperatorSdkApiClientImpl(apiClient)
            val informerFactory = SharedInformerFactory(apiClient)

            val operatorController = OperatorSdkControllerImpl(informerFactory)

            val callGenerator = CallGeneratorUtils.customNamedCRDCallGenerator(
                apiClient,
                namespace,
                "app.brvith.com",
                "v1alpha1",
                "selfservices"
            )

            val sharedIndexInformer =
                SharedInformerUtils.createSharedInformer<SampleCRD, SampleCRDList>(
                    informerFactory,
                    callGenerator
                )

            informerFactory.startAllRegisteredInformers()

            val nodeReconciler = SampleCRDReconciler(operatorApiClient, sharedIndexInformer)

            val watchBlock = fun(workQueue: WorkQueue<Request>): ControllerWatch<SampleCRD> {
                return ControllerBuilder.controllerWatchBuilder(SampleCRD::class.java, workQueue)
                    // .withWorkQueueKeyFunc { node: SampleCRD ->
                    //     val key = "${node.metadata.namespace}/${node.metadata.name}"
                    //     Request(key)
                    // }
                    .withOnAddFilter { createdNode: SampleCRD ->
                        log.info("&&&& Adding SampleCRD :${createdNode.kind},${createdNode.metadata.name}")
                        true
                    }
                    .withOnUpdateFilter { oldNode: SampleCRD, newNode: SampleCRD ->
                        log.info("&&&& Updated SampleCRD : ${Yaml.dump(newNode)}")
                        oldNode.metadata.resourceVersion != newNode.metadata.resourceVersion
                    }
                    .withOnDeleteFilter { deletedNode: SampleCRD, stateUnknown: Boolean ->
                        log.info("&&&& Deleting SampleCRD : ${deletedNode.kind}, ${deletedNode.metadata.name}")
                        true
                    }
                    .build()
            }

            val readyFunc = Supplier<Boolean> {
                sharedIndexInformer.hasSynced()
            }

            val controller = operatorController.createController<SampleCRD>(
                "samplecrd-controller", nodeReconciler, watchBlock,
                readyFunc, 10
            )

            /** Trigger Namespace create and Delete with random interval **/
            launch(Dispatchers.IO) {
                async {
                    val deleteResult = crdClient.delete(cr.metadata.namespace, cr.metadata.name)
                    log.info("Deleted ${deleteResult.httpStatusCode} ")
                    val createdResult = crdClient.create(cr)
                    log.info("Created ${createdResult.httpStatusCode} : ${createdResult.`object`}")
                    delay(10000)
                    val createdObject = createdResult.`object`
                    createdObject.spec.size = 2
                    val updated = crdClient.update(createdObject)
                    log.info("Updated  ${updated.status} : ${updated.`object`}")
                }
            }

            val controllers = arrayListOf<Controller>(controller)
            log.info("######## Starting Controller .....")
            operatorController.startController(controllers, "samplecrd-controller")
        }
    }
}