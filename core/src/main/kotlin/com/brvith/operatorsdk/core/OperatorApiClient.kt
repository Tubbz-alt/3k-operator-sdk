package com.brvith.operatorsdk.core

import com.brvith.operatorsdk.core.utils.ApiClientUtils
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.extended.generic.KubernetesApiResponse
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.ApiextensionsV1beta1Api
import io.kubernetes.client.openapi.apis.AppsV1Api
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api
import io.kubernetes.client.openapi.models.V1ClusterRole
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding
import io.kubernetes.client.openapi.models.V1ClusterRoleBindingList
import io.kubernetes.client.openapi.models.V1ClusterRoleList
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1DeploymentList
import io.kubernetes.client.openapi.models.V1ListMeta
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1NamespaceList
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList
import io.kubernetes.client.openapi.models.V1Role
import io.kubernetes.client.openapi.models.V1RoleBinding
import io.kubernetes.client.openapi.models.V1RoleBindingList
import io.kubernetes.client.openapi.models.V1RoleList
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.openapi.models.V1ServiceAccount
import io.kubernetes.client.openapi.models.V1ServiceAccountList
import io.kubernetes.client.openapi.models.V1ServiceList
import io.kubernetes.client.openapi.models.V1ServiceSpec
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionList
import io.kubernetes.client.util.Watch
import io.kubernetes.client.util.Yaml
import java.io.File
import java.lang.reflect.Type

interface OperatorSdkApiClient {

    fun apiClient(): ApiClient

    suspend fun deploy(file: File)

    suspend fun createNamespace(namespace: String): V1Namespace

    suspend fun deleteNamespace(namespace: String)

    suspend fun deployServiceAccount(namespace: String, serviceAccount: File)

    suspend fun deployServiceAccount(namespace: String, serviceAccount: V1ServiceAccount): V1ServiceAccount

    suspend fun unDeployServiceAccount(namespace: String, name: String)

    suspend fun deployRole(namespace: String, role: V1Role): V1Role

    suspend fun unDeployRole(namespace: String, name: String)

    suspend fun deployClusterRole(clusterRole: V1ClusterRole): V1ClusterRole

    suspend fun unDeployClusterRole(name: String)

    suspend fun deployRole(namespace: String, role: File)

    suspend fun deployRoleBinding(namespace: String, roleBinding: V1RoleBinding): V1RoleBinding

    suspend fun unDeployRoleBinding(namespace: String, name: String)

    suspend fun deployClusterRoleBinding(clusterRoleBinding: V1ClusterRoleBinding): V1ClusterRoleBinding

    suspend fun unDeployClusterRoleBinding(name: String)

    suspend fun deployRoleBinding(namespace: String, roleBinding: File)

    suspend fun deployCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition)
        : V1beta1CustomResourceDefinition

    suspend fun unDeployCustomResourceDefinition(name: String)

    suspend fun deployCustomResourceDefinition(customResourceDefinition: File)

    suspend fun deployDeployment(namespace: String, operators: V1Deployment): V1Deployment

    suspend fun unDeployDeployment(namespace: String, name: String)

    suspend fun deployService(namespace: String, operators: V1Service): V1Service

    suspend fun unDeployService(namespace: String, name: String)
}

class OperatorSdkApiClientImpl(val client: ApiClient) : OperatorSdkApiClient {
    private val api: CoreV1Api = CoreV1Api()
    private val appsApi = AppsV1Api(client)
    private val rbacAuthorizationV1Api = RbacAuthorizationV1Api(client)
    private val extensionApi = ApiextensionsV1beta1Api(client)

    val log = logger(OperatorSdkApiClientImpl::class)

    override fun apiClient(): ApiClient {
        return client
    }

    override suspend fun deploy(file: File) {
        val resource = Yaml.load(file)
        when (resource) {
            is V1Pod -> {
                TODO()
            }
        }
    }

    override suspend fun createNamespace(namespace: String): V1Namespace {
        val result = safeCreateViaApi {
            val namespace = V1Namespace().metadata(V1ObjectMeta().name(namespace))
            namespaceApiClient().create(namespace)
        }.`object`
        log.info("Namespace(${result.metadata!!.name}) created")
        return result
    }

    override suspend fun deployServiceAccount(namespace: String, serviceAccount: V1ServiceAccount): V1ServiceAccount {
        val result = safeCreateViaApi {
            serviceAccount.metadata!!.namespace = namespace
            serviceAccountApiClient().create(serviceAccount)
        }.`object`
        log.info("ServiceAccount(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun deployServiceAccount(namespace: String, serviceAccount: File) {
        val sa = Yaml.load(serviceAccount) as V1ServiceAccount
        deployServiceAccount(namespace, sa)
    }

    /** Role */
    override suspend fun deployRole(namespace: String, role: V1Role): V1Role {
        val result = safeCreateViaApi {
            role.metadata!!.namespace = namespace
            roleApiClient().create(role)
        }.`object`
        log.info("Role(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun deployClusterRole(clusterRole: V1ClusterRole): V1ClusterRole {
        val result = safeCreateViaApi {
            clusterRoleApiClient().create(clusterRole)
        }.`object`
        log.info("ClusterRole(${clusterRole.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun deployRole(namespace: String, role: File) {
        val r = Yaml.load(role)
        when (r) {
            is V1Role -> deployRole(namespace, r)
            is V1ClusterRole -> deployClusterRole(r)
            else -> throw Exception("Role Type(${r::class.simpleName}) unknown, it should be  Role or ClusterRole")
        }
    }

    override suspend fun deployRoleBinding(namespace: String, roleBinding: V1RoleBinding): V1RoleBinding {
        val result = safeCreateViaApi {
            roleBinding.metadata!!.namespace = namespace
            roleBindingsApiClient().create(roleBinding)
        }.`object`
        log.info("RoleBinding(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun deployClusterRoleBinding(clusterRoleBinding: V1ClusterRoleBinding): V1ClusterRoleBinding {
        val result = safeCreateViaApi {
            clusterRoleBindingApiClient().create(clusterRoleBinding)
        }.`object`
        log.info("ClusterRoleBinding(${result.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun deployRoleBinding(namespace: String, roleBinding: File) {
        val rb = Yaml.load(roleBinding)
        when (rb) {
            is V1RoleBinding -> deployRoleBinding(namespace, rb)
            is V1ClusterRoleBinding -> deployClusterRoleBinding(rb)
            else -> throw Exception("RoleBinding Type(${rb::class.simpleName}) unknown, it should be  RoleBinding or ClusterRoleBinding")
        }
    }

    override suspend fun deployCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition): V1beta1CustomResourceDefinition {
        val result = safeCreateViaApi {
            customResourceDefinitionsApiClient().create(customResourceDefinition)
        }.`object`
        log.info("CustomResourceDefinition(${result.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun deployCustomResourceDefinition(customResourceDefinition: File) {
        val crd = Yaml.load(customResourceDefinition) as V1beta1CustomResourceDefinition
        deployCustomResourceDefinition(crd)
    }

    override suspend fun deployDeployment(namespace: String, deployment: V1Deployment): V1Deployment {
        val result = safeCreateViaApi {
            deployment.metadata!!.namespace = namespace
            deploymentApiClient().create(deployment)
        }.`object`
        log.info("Deployment(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun deployService(namespace: String, service: V1Service): V1Service {
        val result = safeCreateViaApi {
            service.metadata!!.namespace = namespace
            serviceApiClient().create(service)
        }.`object`
        log.info("Service(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun deleteNamespace(namespace: String) {
        safeDeleteViaApi {
            val response = namespaceApiClient().delete(namespace)
            log.info("Namespace($namespace) deleted(${response.httpStatusCode})")
            response
        }
    }

    override suspend fun unDeployServiceAccount(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            serviceAccountApiClient().delete(namespace, name)
        }
        log.info("Service Account($namespace) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployRole(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            roleApiClient().delete(namespace, name)
        }
        log.info("Role($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployClusterRole(name: String) {
        val result = safeDeleteViaApi {
            clusterRoleApiClient().delete(name)
        }
        log.info("ClusterRole($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployRoleBinding(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            roleBindingsApiClient().delete(namespace, name)
        }
        log.info("RoleBinding($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployClusterRoleBinding(name: String) {
        val result = safeDeleteViaApi {
            clusterRoleBindingApiClient().delete(name)
        }
        log.info("ClusterRoleBinding($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployCustomResourceDefinition(name: String) {
        val result = safeDeleteViaApi {
            customResourceDefinitionsApiClient().delete(name)
        }
        log.info("CustomResourceDefinition($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployDeployment(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            deploymentApiClient().delete(namespace, name)
        }
        log.info("Deployment($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun unDeployService(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            serviceApiClient().delete(namespace, name)
        }
        log.info("Service($namespace, $name) deleted(${result.httpStatusCode})")
    }

    suspend fun <T> watch(watchType: Type): Watch<T> {
        return Watch.createWatch(
            client,
            api.listNamespaceCall(
                null, null, null, null, null, 5,
                null, null, true, null
            ),
            watchType
        )
    }

    suspend fun <T> safeDeleteViaApi(block: () -> KubernetesApiResponse<T>): KubernetesApiResponse<T> {
        return try {
            val response = block()
            if (!response.isSuccess && response.httpStatusCode != 404) {
                throw Exception("##### Failed : ${response.status}")
            } else {
                response
            }
        } catch (e: java.lang.Exception) {
            throw(e)
        }
    }

    suspend fun <T> safeCreateViaApi(block: () -> KubernetesApiResponse<T>): KubernetesApiResponse<T> {
        return try {
            val response = block()
            if (!response.isSuccess) {
                throw Exception("##### Failed : ${response.status.message}")
            } else {
                response
            }
        } catch (e: java.lang.Exception) {
            throw(e)
        }
    }

    fun namespaceApiClient(): GenericKubernetesApi<V1Namespace, V1NamespaceList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "namespaces")
    }

    fun podApiClient(): GenericKubernetesApi<V1Pod, V1PodList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "pods")
    }

    fun roleApiClient(): GenericKubernetesApi<V1Role, V1RoleList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "roles")
    }

    fun clusterRoleApiClient(): GenericKubernetesApi<V1ClusterRole, V1ClusterRoleList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "clusterroles")
    }

    fun roleBindingsApiClient(): GenericKubernetesApi<V1RoleBinding, V1RoleBindingList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "rolebindings")
    }

    fun clusterRoleBindingApiClient(): GenericKubernetesApi<V1ClusterRoleBinding, V1ClusterRoleBindingList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "clusterrolebindings")
    }

    fun serviceAccountApiClient(): GenericKubernetesApi<V1ServiceAccount, V1ServiceAccountList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "serviceaccounts")
    }

    fun deploymentApiClient(): GenericKubernetesApi<V1Deployment, V1DeploymentList> {
        return ApiClientUtils.resourceApiClient(client, "apps", "v1", "deployments")
    }

    fun serviceApiClient(): GenericKubernetesApi<V1Service, V1ServiceList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "services")
    }

    fun customResourceDefinitionsApiClient()
        : GenericKubernetesApi<V1beta1CustomResourceDefinition, V1beta1CustomResourceDefinitionList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "apiextensions.k8s.io",
            "v1beta1",
            "customresourcedefinitions"
        )
    }
}

open class OperatorSdkCRD {
    lateinit var apiVersion: String
    lateinit var kind: String
    lateinit var metadata: V1ObjectMeta
}

open class OperatorSdkCRDList<ApiType> {
    lateinit var metadata: V1ListMeta
    var items: List<ApiType>? = null
}
