package com.brvith.operatorsdk.core.sample

import com.brvith.operatorsdk.core.logger
import io.kubernetes.client.extended.controller.reconciler.Reconciler
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.controller.reconciler.Result
import io.kubernetes.client.extended.event.EventType
import io.kubernetes.client.extended.event.legacy.EventRecorder
import io.kubernetes.client.extended.event.legacy.LegacyEventBroadcaster
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.cache.Lister
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1EventSource
import io.kubernetes.client.openapi.models.V1Namespace
import kotlinx.coroutines.runBlocking

open class V1NamespaceReconciler(
    private val client: ApiClient,
    private val sharedIndexInformer: SharedIndexInformer<V1Namespace>
) : Reconciler {

    private val log = logger(V1NamespaceReconciler::class)

    private val coreV1Api: CoreV1Api = CoreV1Api(client)
    lateinit var lister: Lister<V1Namespace>
    lateinit var eventRecorder: EventRecorder

    open suspend fun initLister() {
        if (!::lister.isInitialized) {
            lister = Lister<V1Namespace>(sharedIndexInformer.indexer)
            log.info("********** Initialized Lister ********")
        }
    }

    open suspend fun initEventRecoder() {
        if (!::eventRecorder.isInitialized) {
            val eventBroadcaster = LegacyEventBroadcaster(coreV1Api)
            eventRecorder = eventBroadcaster.newRecorder(
                V1EventSource().host("localhost").component("node-printer")
            )
            log.info("********** Initialized Event Broad Caster ********")
        }
    }

    override fun reconcile(request: Request): Result {

        runBlocking {
            initLister()
            initEventRecoder()
        }

        val namespace = lister.get(request.name)
        log.info("######## Reconciler triggered : $namespace ")

        eventRecorder.event(
            namespace,
            EventType.Normal,
            "Print Node",
            "Successfully printed %s",
            request.name
        )
        return Result(false)
    }
}