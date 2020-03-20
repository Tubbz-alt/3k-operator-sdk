package controller

import apis.app.v1alpha1.KKKCdr
import apis.app.v1alpha1.KKKCdrList
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
            SharedInformerUtils.createSharedInformer<KKKCdr, KKKCdrList>(
                informerFactory,
                callGenerator
            )

        /** Start all the registered Informers */
        informerFactory.startAllRegisteredInformers()

        val nodeReconciler = KKKCrdReconciler(
            operatorApiClient,
            sharedIndexInformer
        )

        val watchBlock = fun(workQueue: WorkQueue<Request>): ControllerWatch<KKKCdr> {
            return ControllerBuilder.controllerWatchBuilder(KKKCdr::class.java, workQueue)
                // .withWorkQueueKeyFunc { node: KKKCdr ->
                //     val key = "${node.metadata.namespace}/${node.metadata.name}"
                //     Request(key)
                // }
                .withOnAddFilter { createdNode: KKKCdr ->
                    log.debug("Watch Adding CRD :${createdNode.kind},${createdNode.metadata.name}")
                    true
                }
                .withOnUpdateFilter { oldNode: KKKCdr, newNode: KKKCdr ->
                    log.info("Watch Updated CRD : ${newNode.asYaml()}")
                    oldNode.metadata.resourceVersion != newNode.metadata.resourceVersion
                }
                .withOnDeleteFilter { deletedNode: KKKCdr, stateUnknown: Boolean ->
                    log.info("Watch Deleting CRD : ${deletedNode.kind}, ${deletedNode.metadata.name}")
                    true
                }
                .build()
        }

        val readyFunc = Supplier<Boolean> {
            sharedIndexInformer.hasSynced()
        }

        /** Create CRD Controller */
        val controller = operatorSdkController.createController<KKKCdr>(
            "kkkcrd-controller", nodeReconciler, watchBlock,
            readyFunc, 10
        )
        val controllers = arrayListOf<Controller>(controller)

        /** Start the controller */
        operatorSdkController.startController(controllers, "kkkcrd-controller")
    }
}