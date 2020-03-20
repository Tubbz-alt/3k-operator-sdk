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

import io.kubernetes.client.informer.ListerWatcher
import io.kubernetes.client.informer.SharedIndexInformer
import io.kubernetes.client.informer.SharedInformerFactory
import io.kubernetes.client.util.CallGenerator

object SharedInformerUtils {

    inline fun <reified ApiType, reified ApiListType> createSharedInformer(
        informerFactory: SharedInformerFactory,
        callGenerator: CallGenerator,
        resyncPeriodMillis: Long = 60000
    )
        : SharedIndexInformer<ApiType> {
        val type = ApiType::class.java
        val listType = ApiListType::class.java
        val sharedIndexInformer = informerFactory.sharedIndexInformerFor(
            callGenerator,
            type,
            listType,
            resyncPeriodMillis
        )
        return sharedIndexInformer
    }
}