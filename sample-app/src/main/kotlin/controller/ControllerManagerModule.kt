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

package controller

import com.brvith.frameworks.operator.OperatorControllerManager
import io.ktor.application.Application
import io.ktor.application.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

/**
 * @author brindasanth
 */

fun Application.controllerManagerModule() {

    /** Run the Controller Manager in separate thread */
    val deferred = async(Dispatchers.IO) {
        try {
            val operatorControllerManager by kodein().instance<OperatorControllerManager>()

            val kkkCrdController by kodein().instance<KKKCrdController>()

            kkkCrdController.createController(operatorControllerManager)

            /** Start the controller */
            operatorControllerManager.startControllers("operator-test")
        } catch (e: Exception) {
            log.error("Failed to start Operator Controller Manager", e)
        }
    }
    deferred.start()
}