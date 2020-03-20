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
import com.brvith.operatorsdk.core.sample.SampleCRDReconciler
import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.controller.reconciler.Result
import io.kubernetes.client.extended.event.legacy.EventRecorder
import io.kubernetes.client.extended.event.legacy.LegacyEventBroadcaster
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.cache.Lister
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1EventSource

open class OperatorSdkReconciler(
    private val operatorSdkApiClient: OperatorSdkApiClient,
    private val sharedIndexInformer: SharedIndexInformer<SampleCRD>
) : Reconciler {

    private val log = logger(SampleCRDReconciler::class)

    val coreV1Api: CoreV1Api = CoreV1Api(operatorSdkApiClient.apiClient())

    lateinit var lister: Lister<SampleCRD>
    lateinit var eventRecorder: EventRecorder

    open suspend fun initLister() {
        if (!::lister.isInitialized) {
            lister = Lister<SampleCRD>(sharedIndexInformer.indexer)
            log.info("********** Initialized Lister ********")
        }
    }

    open suspend fun initEventRecoder() {
        if (!::eventRecorder.isInitialized) {
            val eventBroadcaster = LegacyEventBroadcaster(coreV1Api)
            eventRecorder = eventBroadcaster.newRecorder(
                V1EventSource().host("localhost").component("sample-crd")
            )
            log.info("********** Initialized Event Broad Caster ********")
        }
    }

    override fun reconcile(request: Request): Result {
        TODO("Not Implemented")
    }
}