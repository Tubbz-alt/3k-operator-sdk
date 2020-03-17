package com.brvith.operatorsdk.core.utils

import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.models.V1Namespace
import io.kubernetes.client.openapi.models.V1NamespaceList
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList

object ApiClientUtils {

    inline fun <reified ApiType, reified ApiTypeList> resourceApiClient(
        apiClient: ApiClient,
        apiGroup: String,
        apiVersion: String,
        resourcePlural: String
    ): GenericKubernetesApi<ApiType, ApiTypeList> {
        val type = ApiType::class.java
        val listType = ApiTypeList::class.java
        return GenericKubernetesApi<ApiType, ApiTypeList>(
            type,
            listType,
            apiGroup,
            apiVersion,
            resourcePlural,
            apiClient
        )
    }

    inline fun <reified ApiType, reified ApiTypeList> resourceApiClient(
        apiClient: ApiClient,
        apiVersion: String,
        resourcePlural: String
    ): GenericKubernetesApi<ApiType, ApiTypeList> {
        return resourceApiClient(
            apiClient,
            "",
            apiVersion,
            resourcePlural
        )
    }

    fun namespaceApiClient(apiClient: ApiClient): GenericKubernetesApi<V1Namespace, V1NamespaceList> {
        return resourceApiClient(apiClient, "v1", "namespaces")
    }

    fun podApiClient(apiClient: ApiClient): GenericKubernetesApi<V1Pod, V1PodList> {
        return resourceApiClient(apiClient, "v1", "pods")
    }
}