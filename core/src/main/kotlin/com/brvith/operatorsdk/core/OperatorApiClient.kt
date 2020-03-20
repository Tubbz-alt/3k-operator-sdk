package com.brvith.operatorsdk.core

import com.brvith.operatorsdk.core.utils.ApiClientUtils
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.extended.generic.KubernetesApiResponse
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.models.V1ClusterRole
import io.kubernetes.client.openapi.models.V1ClusterRoleBinding
import io.kubernetes.client.openapi.models.V1ClusterRoleBindingList
import io.kubernetes.client.openapi.models.V1ClusterRoleList
import io.kubernetes.client.openapi.models.V1ConfigMap
import io.kubernetes.client.openapi.models.V1ConfigMapList
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionList
import io.kubernetes.client.openapi.models.V1Deployment
import io.kubernetes.client.openapi.models.V1DeploymentList
import io.kubernetes.client.openapi.models.V1Event
import io.kubernetes.client.openapi.models.V1EventList
import io.kubernetes.client.openapi.models.V1Job
import io.kubernetes.client.openapi.models.V1JobList
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1NamespaceList
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList
import io.kubernetes.client.openapi.models.V1Role
import io.kubernetes.client.openapi.models.V1RoleBinding
import io.kubernetes.client.openapi.models.V1RoleBindingList
import io.kubernetes.client.openapi.models.V1RoleList
import io.kubernetes.client.openapi.models.V1Secret
import io.kubernetes.client.openapi.models.V1SecretList
import io.kubernetes.client.openapi.models.V1Service
import io.kubernetes.client.openapi.models.V1ServiceAccount
import io.kubernetes.client.openapi.models.V1ServiceAccountList
import io.kubernetes.client.openapi.models.V1ServiceList
import io.kubernetes.client.openapi.models.V1Status
import io.kubernetes.client.openapi.models.V1alpha1AuditSink
import io.kubernetes.client.openapi.models.V1alpha1AuditSinkList
import io.kubernetes.client.openapi.models.V1beta1CronJob
import io.kubernetes.client.openapi.models.V1beta1CronJobList
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionList
import io.kubernetes.client.util.Yaml
import java.io.File

interface OperatorSdkApiClient {

    fun apiClient(): ApiClient

    /** File apply */
    suspend fun apply(file: File)

    suspend fun applyServiceAccount(namespace: String, serviceAccount: File)

    suspend fun applyRole(namespace: String, role: File)

    suspend fun applyRoleBinding(namespace: String, roleBinding: File)

    suspend fun applyCustomResourceDefinition(customResourceDefinition: File)

    /** Namespace */
    suspend fun createNamespace(namespace: String): V1Namespace

    suspend fun deleteNamespace(namespace: String)

    /** Service Account */
    suspend fun createServiceAccount(namespace: String, serviceAccount: V1ServiceAccount): V1ServiceAccount

    suspend fun deleteServiceAccount(namespace: String, name: String)

    /** Role */
    suspend fun createRole(namespace: String, role: V1Role): V1Role

    suspend fun deleteRole(namespace: String, name: String)

    suspend fun createClusterRole(clusterRole: V1ClusterRole): V1ClusterRole

    suspend fun deleteClusterRole(name: String)

    /** Role Binding */
    suspend fun createRoleBinding(namespace: String, roleBinding: V1RoleBinding): V1RoleBinding

    suspend fun deleteRoleBinding(namespace: String, name: String)

    suspend fun createClusterRoleBinding(clusterRoleBinding: V1ClusterRoleBinding): V1ClusterRoleBinding

    suspend fun deleteClusterRoleBinding(name: String)

    /** Custom Resource Definitions */
    suspend fun createCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition)
        : V1beta1CustomResourceDefinition

    suspend fun deleteCustomResourceDefinition(name: String)

    /** Deployment */
    suspend fun createDeployment(namespace: String, operators: V1Deployment): V1Deployment

    suspend fun deleteDeployment(namespace: String, name: String)

    /** Service */
    suspend fun createService(namespace: String, operators: V1Service): V1Service

    suspend fun deleteService(namespace: String, name: String)

    /** API **/
    fun namespaceApiClient(): GenericKubernetesApi<V1Namespace, V1NamespaceList>

    fun podApiClient(): GenericKubernetesApi<V1Pod, V1PodList>

    fun roleApiClient(): GenericKubernetesApi<V1Role, V1RoleList>

    fun clusterRoleApiClient(): GenericKubernetesApi<V1ClusterRole, V1ClusterRoleList>

    fun roleBindingsApiClient(): GenericKubernetesApi<V1RoleBinding, V1RoleBindingList>

    fun clusterRoleBindingApiClient(): GenericKubernetesApi<V1ClusterRoleBinding, V1ClusterRoleBindingList>

    fun serviceAccountApiClient(): GenericKubernetesApi<V1ServiceAccount, V1ServiceAccountList>

    fun deploymentApiClient(): GenericKubernetesApi<V1Deployment, V1DeploymentList>

    fun serviceApiClient(): GenericKubernetesApi<V1Service, V1ServiceList>

    fun customResourceDefinitionsApiClient()
        : GenericKubernetesApi<V1CustomResourceDefinition, V1CustomResourceDefinitionList>

    fun betaCustomResourceDefinitionsApiClient()
        : GenericKubernetesApi<V1beta1CustomResourceDefinition, V1beta1CustomResourceDefinitionList>

    fun configMapApiClient(): GenericKubernetesApi<V1ConfigMap, V1ConfigMapList>

    fun eventApiClient(): GenericKubernetesApi<V1Event, V1EventList>

    fun secretApiClient(): GenericKubernetesApi<V1Secret, V1SecretList>

    fun jobApiClient(): GenericKubernetesApi<V1Job, V1JobList>

    fun cronJobApiClient(): GenericKubernetesApi<V1beta1CronJob, V1beta1CronJobList>

    fun auditSinkApiClient(): GenericKubernetesApi<V1alpha1AuditSink, V1alpha1AuditSinkList>
}

abstract class AbstractOperatorSdkApiClient(val client: ApiClient) : OperatorSdkApiClient

class OperatorSdkApiClientImpl(client: ApiClient) : AbstractOperatorSdkApiClient(client) {
    val log = logger(OperatorSdkApiClientImpl::class)

    override fun apiClient(): ApiClient {
        return client
    }

    override suspend fun apply(file: File) {
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

    override suspend fun createServiceAccount(namespace: String, serviceAccount: V1ServiceAccount): V1ServiceAccount {
        val result = safeCreateViaApi {
            serviceAccount.metadata!!.namespace = namespace
            serviceAccountApiClient().create(serviceAccount)
        }.`object`
        log.info("ServiceAccount(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun applyServiceAccount(namespace: String, serviceAccount: File) {
        val sa = serviceAccount.asYamlObject<V1ServiceAccount>()
        createServiceAccount(namespace, sa)
    }

    /** Role */
    override suspend fun createRole(namespace: String, role: V1Role): V1Role {
        val result = safeCreateViaApi {
            role.metadata!!.namespace = namespace
            roleApiClient().create(role)
        }.`object`
        log.info("Role(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun createClusterRole(clusterRole: V1ClusterRole): V1ClusterRole {
        val result = safeCreateViaApi {
            clusterRoleApiClient().create(clusterRole)
        }.`object`
        log.info("ClusterRole(${clusterRole.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun applyRole(namespace: String, role: File) {
        val r = Yaml.load(role)
        when (r) {
            is V1Role -> createRole(namespace, r)
            is V1ClusterRole -> createClusterRole(r)
            else -> throw Exception("Role Type(${r::class.simpleName}) unknown, it should be  Role or ClusterRole")
        }
    }

    override suspend fun createRoleBinding(namespace: String, roleBinding: V1RoleBinding): V1RoleBinding {
        val result = safeCreateViaApi {
            roleBinding.metadata!!.namespace = namespace
            roleBindingsApiClient().create(roleBinding)
        }.`object`
        log.info("RoleBinding(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun createClusterRoleBinding(clusterRoleBinding: V1ClusterRoleBinding): V1ClusterRoleBinding {
        val result = safeCreateViaApi {
            clusterRoleBindingApiClient().create(clusterRoleBinding)
        }.`object`
        log.info("ClusterRoleBinding(${result.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun applyRoleBinding(namespace: String, roleBinding: File) {
        val rb = Yaml.load(roleBinding)
        when (rb) {
            is V1RoleBinding -> createRoleBinding(namespace, rb)
            is V1ClusterRoleBinding -> createClusterRoleBinding(rb)
            else -> throw Exception("RoleBinding Type(${rb::class.simpleName}) unknown, it should be  RoleBinding or ClusterRoleBinding")
        }
    }

    override suspend fun createCustomResourceDefinition(customResourceDefinition: V1beta1CustomResourceDefinition): V1beta1CustomResourceDefinition {
        val result = safeCreateViaApi {
            betaCustomResourceDefinitionsApiClient().create(customResourceDefinition)
        }.`object`
        log.info("CustomResourceDefinition(${result.metadata!!.name}) deployed successfully.")
        return result
    }

    override suspend fun applyCustomResourceDefinition(customResourceDefinition: File) {
        val crd = customResourceDefinition.asYamlObject<V1beta1CustomResourceDefinition>()
        createCustomResourceDefinition(crd)
    }

    override suspend fun createDeployment(namespace: String, deployment: V1Deployment): V1Deployment {
        val result = safeCreateViaApi {
            deployment.metadata!!.namespace = namespace
            deploymentApiClient().create(deployment)
        }.`object`
        log.info("Deployment(name = ${result.metadata!!.name}, ns = ${result.metadata!!.namespace}) deployed successfully.")
        return result
    }

    override suspend fun createService(namespace: String, service: V1Service): V1Service {
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

    override suspend fun deleteServiceAccount(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            serviceAccountApiClient().delete(namespace, name)
        }
        log.info("Service Account($namespace) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteRole(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            roleApiClient().delete(namespace, name)
        }
        log.info("Role($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteClusterRole(name: String) {
        val result = safeDeleteViaApi {
            clusterRoleApiClient().delete(name)
        }
        log.info("ClusterRole($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteRoleBinding(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            roleBindingsApiClient().delete(namespace, name)
        }
        log.info("RoleBinding($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteClusterRoleBinding(name: String) {
        val result = safeDeleteViaApi {
            clusterRoleBindingApiClient().delete(name)
        }
        log.info("ClusterRoleBinding($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteCustomResourceDefinition(name: String) {
        val result = safeDeleteViaApi {
            customResourceDefinitionsApiClient().delete(name)
        }
        log.info("CustomResourceDefinition($name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteDeployment(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            deploymentApiClient().delete(namespace, name)
        }
        log.info("Deployment($namespace, $name) deleted(${result.httpStatusCode})")
    }

    override suspend fun deleteService(namespace: String, name: String) {
        val result = safeDeleteViaApi {
            serviceApiClient().delete(namespace, name)
        }
        log.info("Service($namespace, $name) deleted(${result.httpStatusCode})")
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

    suspend fun <ApiType> extractObject(block: () -> KubernetesApiResponse<ApiType>): ApiType {
        val status = block()
        return status.`object`
    }

    override fun namespaceApiClient(): GenericKubernetesApi<V1Namespace, V1NamespaceList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "namespaces")
    }

    override fun podApiClient(): GenericKubernetesApi<V1Pod, V1PodList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "pods")
    }

    override fun roleApiClient(): GenericKubernetesApi<V1Role, V1RoleList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "roles")
    }

    override fun clusterRoleApiClient(): GenericKubernetesApi<V1ClusterRole, V1ClusterRoleList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "clusterroles")
    }

    override fun roleBindingsApiClient(): GenericKubernetesApi<V1RoleBinding, V1RoleBindingList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "rolebindings")
    }

    override fun clusterRoleBindingApiClient(): GenericKubernetesApi<V1ClusterRoleBinding, V1ClusterRoleBindingList> {
        return ApiClientUtils.resourceApiClient(client, "rbac.authorization.k8s.io", "v1", "clusterrolebindings")
    }

    override fun serviceAccountApiClient(): GenericKubernetesApi<V1ServiceAccount, V1ServiceAccountList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "serviceaccounts")
    }

    override fun deploymentApiClient(): GenericKubernetesApi<V1Deployment, V1DeploymentList> {
        return ApiClientUtils.resourceApiClient(client, "apps", "v1", "deployments")
    }

    override fun serviceApiClient(): GenericKubernetesApi<V1Service, V1ServiceList> {
        return ApiClientUtils.resourceApiClient(client, "v1", "services")
    }

    override fun betaCustomResourceDefinitionsApiClient()
        : GenericKubernetesApi<V1beta1CustomResourceDefinition, V1beta1CustomResourceDefinitionList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "apiextensions.k8s.io",
            "v1beta1",
            "customresourcedefinitions"
        )
    }

    override fun customResourceDefinitionsApiClient()
        : GenericKubernetesApi<V1CustomResourceDefinition, V1CustomResourceDefinitionList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "apiextensions.k8s.io",
            "v1",
            "customresourcedefinitions"
        )
    }

    override fun configMapApiClient(): GenericKubernetesApi<V1ConfigMap, V1ConfigMapList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "",
            "v1",
            "configmaps"
        )
    }

    override fun secretApiClient(): GenericKubernetesApi<V1Secret, V1SecretList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "",
            "v1",
            "secrets"
        )
    }

    override fun jobApiClient(): GenericKubernetesApi<V1Job, V1JobList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "batch",
            "v1",
            "jobs"
        )
    }

    override fun cronJobApiClient(): GenericKubernetesApi<V1beta1CronJob, V1beta1CronJobList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "batch",
            "v1beta1",
            "cronjobs"
        )
    }

    override fun eventApiClient(): GenericKubernetesApi<V1Event, V1EventList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "",
            "v1",
            "events"
        )
    }

    /** Create Audit Backend */
    override fun auditSinkApiClient()
        : GenericKubernetesApi<V1alpha1AuditSink, V1alpha1AuditSinkList> {
        return ApiClientUtils.resourceApiClient(
            client,
            "auditregistration.k8s.io",
            "v1alpha1",
            "auditsinks"
        )
    }
}
