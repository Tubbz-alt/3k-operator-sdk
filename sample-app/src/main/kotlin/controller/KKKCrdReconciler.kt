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

import apis.app.v1alpha1.KKKCdr
import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.asYaml
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
import kotlinx.coroutines.runBlocking

open class KKKCrdReconciler(
    private val operatorSdkApiClient: OperatorSdkApiClient,
    private val sharedIndexInformer: SharedIndexInformer<KKKCdr>
) : Reconciler {

    private val log = logger(KKKCrdReconciler::class)

    private val coreV1Api: CoreV1Api = CoreV1Api(operatorSdkApiClient.apiClient())
    lateinit var lister: Lister<KKKCdr>
    lateinit var eventRecorder: EventRecorder

    open suspend fun initLister() {
        if (!::lister.isInitialized) {
            lister = Lister<KKKCdr>(sharedIndexInformer.indexer)
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

        runBlocking {
            initLister()
            initEventRecoder()
        }

        val key = "${request.namespace}/${request.name}"
        val kkkCrd = lister.get(key)
        log.info("######## Reconciler triggered : ${kkkCrd.asYaml()}")

        notifyStatus(kkkCrd, "PENDING")

        notifyStatus(kkkCrd, "ACTIVE")


        eventRecorder.event(
            kkkCrd,
            EventType.Normal,
            "kkkCrd Reconcile",
            "Successfully reconcile kkkCrd(%s)",
            request.name
        )
        return Result(false)
    }

    fun notifyStatus(KKKCdr: KKKCdr, status: String) {
    }
}