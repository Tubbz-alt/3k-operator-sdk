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

package apis.app.v1alpha1

import com.brvith.frameworks.operator.AbstractOperatorApiClient
import com.brvith.frameworks.operator.OperatorCRD
import com.brvith.frameworks.operator.OperatorCRDList
import com.brvith.frameworks.operator.utils.ApiClientUtils
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionBuilder
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionNamesBuilder
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionSpecBuilder
import io.kubernetes.client.openapi.models.V1CustomResourceDefinitionVersionBuilder
import io.kubernetes.client.openapi.models.V1ObjectMetaBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinition
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionNamesBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionSpecBuilder
import io.kubernetes.client.openapi.models.V1beta1CustomResourceDefinitionVersionBuilder
import io.kubernetes.client.util.Yaml

open class KKKCrd : OperatorCRD() {
    var spec: KKKCrdSpec = KKKCrdSpec()
}

open class KKKCrdList : OperatorCRDList<KKKCrd>()

open class KKKCrdSpec {
    lateinit var vnfName: String
    lateinit var vnfId: String
    var size: Int = 1
}

fun kkkCrdBeta1CustomResourceDefinition(): V1beta1CustomResourceDefinition {
    return V1beta1CustomResourceDefinitionBuilder()
        .withApiVersion("apiextensions.k8s.io/v1beta1")
        .withKind("CustomResourceDefinition")
        .withMetadata(
            V1ObjectMetaBuilder()
                .withName("kkkcrds.app.brvith.com")
                .build()
        )
        .withSpec(
            V1beta1CustomResourceDefinitionSpecBuilder()
                .withGroup("app.brvith.com")
                .withVersion("v1alpha1")
                .withVersions(
                    V1beta1CustomResourceDefinitionVersionBuilder()
                        .withName("v1alpha1")
                        .withServed(true)
                        .withStorage(true)
                        .build()
                )
                .withNames(
                    V1beta1CustomResourceDefinitionNamesBuilder()
                        .withKind("KKKCrd")
                        .withListKind("KKKCrdList")
                        .withSingular("kkkcrd")
                        .withNewPlural("kkkcrds")
                        .build()
                )
                .withScope("Namespaced")
                .build()
        )
        .build()
}

fun kkkCrdCustomResourceDefinition(): V1CustomResourceDefinition {
    return V1CustomResourceDefinitionBuilder()
        .withApiVersion("apiextensions.k8s.io/v1")
        .withKind("CustomResourceDefinition")
        .withMetadata(
            V1ObjectMetaBuilder()
                .withName("kkkcrds.app.brvith.com")
                .build()
        )
        .withSpec(
            V1CustomResourceDefinitionSpecBuilder()
                .withGroup("app.brvith.com")
                .withVersions(
                    V1CustomResourceDefinitionVersionBuilder()
                        .withName("v1alpha1")
                        .withServed(true)
                        .withStorage(true)
                        .build()
                )
                .withNames(
                    V1CustomResourceDefinitionNamesBuilder()
                        .withKind("KKKCrd")
                        .withListKind("KKKCrdList")
                        .withSingular("kkkcrd")
                        .withNewPlural("kkkcrds")
                        .build()
                )
                .withScope("Namespaced")
                .build()
        )
        .build()
}

fun addCrdTypeToYamlModule() {
    Yaml.addModelMap("app.brvith.com/v1alpha1", "KKKCrd", KKKCrd::class.java)
}

fun AbstractOperatorApiClient.kkkCrdApiClient(): GenericKubernetesApi<KKKCrd, KKKCrdList> {
    return ApiClientUtils.resourceApiClient(
        client,
        "app.brvith.com",
        "v1alpha1",
        "kkkcrds"
    )
}