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

package com.brvith.operatorsdk.core.sample

import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkReconciler
import com.brvith.operatorsdk.core.logger
import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.controller.reconciler.Result
import io.kubernetes.client.extended.event.EventType
import io.kubernetes.client.extended.event.legacy.EventRecorder
import io.kubernetes.client.extended.event.legacy.LegacyEventBroadcaster
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.cache.Lister
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1EventSource
import io.kubernetes.client.util.Yaml
import kotlinx.coroutines.runBlocking

open class SampleCRDReconciler(
    operatorSdkApiClient: OperatorSdkApiClient,
    sharedIndexInformer: SharedIndexInformer<SampleCRD>
) : OperatorSdkReconciler(operatorSdkApiClient, sharedIndexInformer) {

    private val log = logger(SampleCRDReconciler::class)

    override fun reconcile(request: Request): Result {

        runBlocking {
            initLister()
            initEventRecoder()
        }

        val key = "${request.namespace}/${request.name}"
        val sampleCRD = lister.get(key)
        log.info("######## Reconciler triggered : ${Yaml.dump(sampleCRD)}")

        eventRecorder.event(
            sampleCRD,
            EventType.Normal,
            "Print Node",
            "Successfully printed %s",
            request.name
        )
        return Result(false)
    }
}