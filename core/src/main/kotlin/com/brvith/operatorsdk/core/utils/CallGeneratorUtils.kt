package com.brvith.operatorsdk.core.utils

import com.brvith.operatorsdk.core.sample.SampleCRD
import com.brvith.operatorsdk.core.sample.SampleCRDList
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.apis.CustomObjectsApi
import io.kubernetes.client.util.CallGenerator

object CallGeneratorUtils {
    fun nameSpaceCallGenerator(): CallGenerator {
        val coreV1Api = CoreV1Api()
        return CallGenerator { params ->
            coreV1Api.listNamespaceCall(
                null,
                null,
                null,
                null,
                null,
                null,
                params.resourceVersion,
                params.timeoutSeconds,
                params.watch,
                null
            )
        }
    }

    fun customNamedCRDCallGenerator(
        apiClient: ApiClient,
        namespace: String,
        apiGroup: String,
        apiVersion: String,
        resourcePlural: String
    ): CallGenerator {
        val customObjectsApi = CustomObjectsApi(apiClient)
        return CallGenerator { params ->
            customObjectsApi.listNamespacedCustomObjectCall(
                apiGroup,
                apiVersion,
                namespace,
                resourcePlural,
                null,
                null,
                null,
                null,
                null,
                params.resourceVersion,
                params.timeoutSeconds,
                params.watch,
                null
            )
        }
    }

    fun customCRDCallGenerator(
        apiClient: ApiClient,
        apiGroup: String,
        apiVersion: String,
        resourcePlural: String
    ): CallGenerator {
        val customObjectsApi = CustomObjectsApi(apiClient)
        return CallGenerator { params ->
            customObjectsApi.listClusterCustomObjectCall(
                apiGroup,
                apiVersion,
                resourcePlural,
                null,
                null,
                null,
                null,
                null,
                params.resourceVersion,
                params.timeoutSeconds,
                params.watch,
                null
            )
        }
    }
}