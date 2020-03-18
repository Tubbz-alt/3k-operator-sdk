package com.brvith.operatorsdk.core

import io.kubernetes.client.openapi.models.V1ListMeta
import io.kubernetes.client.openapi.models.V1ObjectMeta

open class OperatorSdkCRD {
    lateinit var apiVersion: String
    lateinit var kind: String
    lateinit var metadata: V1ObjectMeta
}

open class OperatorSdkCRDList<ApiType> {
    lateinit var metadata: V1ListMeta
    var items: List<ApiType>? = null
}
