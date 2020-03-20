/*
 * Copyright © 2020 Brvith Solutions.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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