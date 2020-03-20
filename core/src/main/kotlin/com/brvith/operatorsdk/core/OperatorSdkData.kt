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
