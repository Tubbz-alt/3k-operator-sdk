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

import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerManager
import io.kubernetes.client.extended.controller.ControllerWatch
import io.kubernetes.client.extended.controller.LeaderElectingController
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig
import io.kubernetes.client.extended.leaderelection.LeaderElector
import io.kubernetes.client.extended.leaderelection.resourcelock.EndpointsLock
import io.kubernetes.client.extended.workqueue.WorkQueue
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.util.CallGenerator
import java.time.Duration
import java.util.function.Supplier

interface OperatorSdkController {

    fun <ApiType> createController(
        name: String,
        reconciler: Reconciler,
        watchBlock: (WorkQueue<Request>) -> ControllerWatch<ApiType>,
        readyFunc: Supplier<Boolean>,
        workerCount: Int
    ): Controller

    fun startController(
        controller: List<Controller>,
        lockName: String,
        leaseDuration: Duration = Duration.ofMillis(10000)
    )
}

open class OperatorSdkControllerImpl(val informerFactory: SharedInformerFactory) :
    OperatorSdkController {
    val log = logger(OperatorSdkApiClientImpl::class)

    override fun <ApiType> createController(
        name: String,
        reconciler: Reconciler,
        watchBlock: (WorkQueue<Request>) -> ControllerWatch<ApiType>,
        readyFunc: Supplier<Boolean>,
        workerCount: Int
    ): Controller {
        return ControllerBuilder.defaultBuilder(informerFactory)
            .withName(name) // optional, set name for controller
            .withReconciler(reconciler) // required, set the actual reconciler
            .withWorkerCount(workerCount) // optional, set worker thread count
            .watch(watchBlock)
            .withReadyFunc(readyFunc)
            .build()
    }

    override fun startController(
        controller: List<Controller>,
        lockName: String,
        leaseDuration: Duration
    ) {
        val controllerManager = ControllerManager(informerFactory, *controller.toTypedArray())
        val leaderElectingController = LeaderElectingController(
            LeaderElector(
                LeaderElectionConfig(
                    EndpointsLock("kube-system", "leader-election", lockName),
                    leaseDuration,
                    Duration.ofMillis(8000),
                    Duration.ofMillis(5000)
                )
            ),
            controllerManager
        )
        log.info("Starting Controller($lockName) ....")
        leaderElectingController.run()
    }
}