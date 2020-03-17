package controller

import com.brvith.operatorsdk.core.OperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkControllerImpl
import com.brvith.operatorsdk.core.asYaml
import com.brvith.operatorsdk.core.logger
import com.brvith.operatorsdk.core.utils.CallGeneratorUtils
import com.brvith.operatorsdk.core.utils.SharedInformerUtils
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.extended.controller.Controller
import io.kubernetes.client.extended.controller.ControllerWatch
import io.kubernetes.client.extended.controller.builder.ControllerBuilder
import io.kubernetes.client.extended.controller.reconciler.Request
import io.kubernetes.client.extended.workqueue.WorkQueue
import java.util.function.Supplier

open class KKKCrdController(private val operatorApiClient: OperatorSdkApiClient) {
    private val log = logger(KKKCrdController::class)

    suspend fun startController(namespace: String) {
        log.info("######## Testing CRD Controller ############")


        val apiClient = operatorApiClient.apiClient()
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
            SharedInformerUtils.createSharedInformer<KKKCdr, KKKCdrList>(
                informerFactory,
                callGenerator
            )

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
                    log.debug("&&&&&&&&&&&&&& Adding SampleCRD :${createdNode.kind},${createdNode.metadata.name}")
                    true
                }
                .withOnUpdateFilter { oldNode: KKKCdr, newNode: KKKCdr ->
                    log.info("&&&&&&&&&&&&&& Updated SampleCRD : ${newNode.asYaml()}")
                    oldNode.metadata.resourceVersion != newNode.metadata.resourceVersion
                }
                .withOnDeleteFilter { deletedNode: KKKCdr, stateUnknown: Boolean ->
                    log.info("&&&&& Deleting SampleCRD : ${deletedNode.kind}, ${deletedNode.metadata.name}")
                    true
                }
                .build()
        }

        val readyFunc = Supplier<Boolean> {
            sharedIndexInformer.hasSynced()
        }

        val controller = operatorController.createController<KKKCdr>(
            "kkcrd-controller", nodeReconciler, watchBlock,
            readyFunc, 10
        )
        val controllers = arrayListOf<Controller>(controller)
        log.info("######## Starting Controller .....")
        operatorController.startController(controllers, "kkcrd-controller")
    }
}