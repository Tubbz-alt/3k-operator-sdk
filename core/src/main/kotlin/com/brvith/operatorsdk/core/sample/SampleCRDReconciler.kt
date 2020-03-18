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