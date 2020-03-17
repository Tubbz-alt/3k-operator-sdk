package com.brvith.operatorsdk.core.sample

import com.brvith.operatorsdk.core.OperatorSdkApiClient
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
    private val operatorSdkApiClient: OperatorSdkApiClient,
    private val sharedIndexInformer: SharedIndexInformer<SampleCRD>
) : Reconciler {

    private val log = logger(SampleCRDReconciler::class)

    private val coreV1Api: CoreV1Api = CoreV1Api(operatorSdkApiClient.apiClient())
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