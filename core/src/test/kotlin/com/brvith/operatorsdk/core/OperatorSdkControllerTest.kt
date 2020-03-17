package com.brvith.operatorsdk.core

import com.brvith.operatorsdk.core.sample.SampleCRD
import com.brvith.operatorsdk.core.sample.SampleCRDList
import com.brvith.operatorsdk.core.sample.SampleCRDReconciler
import com.brvith.operatorsdk.core.sample.V1NamespaceReconciler
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
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1NamespaceList
import io.kubernetes.client.util.CallGenerator
import io.kubernetes.client.util.CallGeneratorParams
import io.kubernetes.client.util.Config
import io.kubernetes.client.util.Yaml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import org.junit.jupiter.api.Test
import java.util.function.Supplier

class OperatorSdkControllerTest {
    private val log = logger(OperatorSdkControllerTest::class)

    // @Test
    fun testOperatorController() {
        runBlocking {
            log.info("######## Testing Controller ############")
            val apiClient: ApiClient = Config.defaultClient()
            Configuration.setDefaultApiClient(apiClient)

            val operator = OperatorSdkApiClientImpl(apiClient)
            val informerFactory = SharedInformerFactory(apiClient)

            val operatorController = OperatorSdkControllerImpl(informerFactory)

            val callGenerator = CallGeneratorUtils.nameSpaceCallGenerator()

            val sharedIndexInformer =
                SharedInformerUtils.createSharedInformer<V1Namespace, V1NamespaceList>(informerFactory, callGenerator)

            informerFactory.startAllRegisteredInformers()

            val nodeReconciler = V1NamespaceReconciler(apiClient, sharedIndexInformer)

            val watchBlock = fun(workQueue: WorkQueue<Request>): ControllerWatch<V1Namespace> {
                return ControllerBuilder.controllerWatchBuilder(V1Namespace::class.java, workQueue)
                    //.withWorkQueueKeyFunc { node: ApiType -> Request(node.metadata!!.name) } // optional, default to
                    .withOnAddFilter { createdNode: V1Namespace ->
                        createdNode.metadata!!.name!!.startsWith("test-namespace")
                    }
                    .withOnUpdateFilter { oldNode: V1Namespace, newNode: V1Namespace ->
                        oldNode.metadata!!.name!!.startsWith("test-namespace")
                    }
                    .withOnDeleteFilter { deletedNode: V1Namespace, stateUnknown: Boolean ->
                        deletedNode.metadata!!.name!!.startsWith("test-namespace")
                    }
                    .build()
            }

            val readyFunc = Supplier<Boolean> {
                sharedIndexInformer.hasSynced()
            }

            val controller = operatorController.createController<V1Namespace>(
                "sample-controller", nodeReconciler, watchBlock,
                readyFunc, 10
            )

            /** Trigger Namespace create and Delete with random interval **/
            launch(Dispatchers.IO) {
                async {
                    repeat(5) {
                        val namespace = "test-namespace$it"
                        try {
                            log.info("######## Started creating namespace : $namespace")
                            operator.createNamespace(namespace)
                            //delay(5000)
                        } catch (e: Exception) {
                        }
                        try {
                            log.info("######## Started deleting namespace : $namespace")
                            operator.deleteNamespace(namespace)
                            delay(5000)
                        } catch (e: Exception) {

                        }
                    }
                }
            }

            val controllers = arrayListOf<Controller>(controller)
            log.info("######## Starting Controller .....")
            operatorController.startController(controllers, "node-list-controller")
        }
    }

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
                        log.debug("&&&&&&&&&&&&&& Adding SampleCRD :${createdNode.kind},${createdNode.metadata.name}")
                        true
                    }
                    .withOnUpdateFilter { oldNode: SampleCRD, newNode: SampleCRD ->
                        log.info("&&&&&&&&&&&&&& Updated SampleCRD : ${Yaml.dump(newNode)}")
                        oldNode.metadata.resourceVersion != newNode.metadata.resourceVersion
                    }
                    .withOnDeleteFilter { deletedNode: SampleCRD, stateUnknown: Boolean ->
                        log.info("&&&&&&&&&&&&&& Deleting SampleCRD : ${deletedNode.kind}, ${deletedNode.metadata.name}")
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
            operatorController.startController(controllers, "node-list-controller")
        }
    }
}