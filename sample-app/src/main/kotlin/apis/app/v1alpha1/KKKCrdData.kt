package apis.app.v1alpha1

import com.brvith.operatorsdk.core.AbstractOperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkCRD
import com.brvith.operatorsdk.core.OperatorSdkCRDList
import com.brvith.operatorsdk.core.utils.ApiClientUtils
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

open class KKKCdr : OperatorSdkCRD() {
    var spec: KKKCdrSpec = KKKCdrSpec()
}

open class KKKCdrList : OperatorSdkCRDList<KKKCdr>()

open class KKKCdrSpec {
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
                        .withListKind("KKKCdrList")
                        .withSingular("kkkcr")
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
                        .withListKind("KKKCdrList")
                        .withSingular("kkkcr")
                        .withNewPlural("kkkcrds")
                        .build()
                )
                .withScope("Namespaced")
                .build()
        )
        .build()
}

fun addCrdTypeToYamlModule() {
    Yaml.addModelMap("app.brvith.com/v1alpha1", "KKKCrd", KKKCdr::class.java)
}

fun AbstractOperatorSdkApiClient.kkkCrdApiClient(): GenericKubernetesApi<KKKCdr, KKKCdrList> {
    return ApiClientUtils.resourceApiClient(
        client,
        "app.brvith.com",
        "v1alpha1",
        "kkkcrds"
    )
}