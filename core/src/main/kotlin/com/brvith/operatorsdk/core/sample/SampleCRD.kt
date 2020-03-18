package com.brvith.operatorsdk.core.sample

import com.brvith.operatorsdk.core.AbstractOperatorSdkApiClient
import com.brvith.operatorsdk.core.OperatorSdkCRD
import com.brvith.operatorsdk.core.OperatorSdkCRDList
import com.brvith.operatorsdk.core.utils.ApiClientUtils
import io.kubernetes.client.extended.generic.GenericKubernetesApi
import io.kubernetes.client.openapi.ApiClient

open class SampleCRD : OperatorSdkCRD() {
    var spec: SampleCRDSpec = SampleCRDSpec()
}

open class SampleCRDList : OperatorSdkCRDList<SampleCRD>() {

}

open class SampleCRDSpec {
    var size: Int = 1
}

fun sampleCRDApiClient(
    client: ApiClient,
    apiGroup: String,
    apiVersion: String,
    resourcePlurals: String
): GenericKubernetesApi<SampleCRD, SampleCRDList> {
    return ApiClientUtils.resourceApiClient(
        client,
        apiGroup,
        apiVersion,
        resourcePlurals
    )
}

fun AbstractOperatorSdkApiClient.sampleCRDApiClient(): GenericKubernetesApi<SampleCRD, SampleCRDList> {
    return ApiClientUtils.resourceApiClient(
        client,
        "app.brvith.com",
        "v1alpha1",
        "selfservices"
    )
}

fun sampleCRDApiClient(client: ApiClient): GenericKubernetesApi<SampleCRD, SampleCRDList> {
    return ApiClientUtils.resourceApiClient(
        client,
        "app.brvith.com",
        "v1alpha1",
        "selfservices"
    )
}


