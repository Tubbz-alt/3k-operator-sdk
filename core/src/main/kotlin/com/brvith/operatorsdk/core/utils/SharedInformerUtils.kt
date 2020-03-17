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