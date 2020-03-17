package controller

import com.brvith.operatorsdk.core.OperatorSdkCRD
import com.brvith.operatorsdk.core.OperatorSdkCRDList
import com.brvith.operatorsdk.core.utils.ApiClientUtils
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.Yaml

open class KKKCdr : OperatorSdkCRD() {
    var spec: KKKCdrSpec = KKKCdrSpec()
}

open class KKKCdrList : OperatorSdkCRDList<KKKCdr>() {

}

open class KKKCdrSpec {
    var size: Int = 1
}

fun addCrdToYamlModule() {
    Yaml.addModelMap("app.brvith.com/v1alpha1", "KKKCrd", KKKCdr::class.java)
}

fun kkkCdrApiClient(
    client: ApiClient,
    apiGroup: String,
    apiVersion: String,
    resourcePlurals: String
): GenericKubernetesApi<KKKCdr, KKKCdrList> {
    return ApiClientUtils.resourceApiClient(
        client,
        apiGroup,
        apiVersion,
        resourcePlurals
    )
}

fun kkkCrdApiClient(client: ApiClient): GenericKubernetesApi<KKKCdr, KKKCdrList> {
    return ApiClientUtils.resourceApiClient(
        client,
        "app.brvith.com",
        "v1alpha1",
        "kkkcrds"
    )
}