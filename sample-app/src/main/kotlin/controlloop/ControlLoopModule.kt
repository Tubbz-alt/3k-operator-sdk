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

package controlloop

import com.brvith.frameworks.operator.logger
import com.brvith.frameworks.operator.OperatorApiClient
import com.brvith.frameworks.operator.logger
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.kubernetes.client.openapi.models.V1alpha1AuditSink
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein

/**
 * @author brindasanth
 */

/** Application Setup */
fun Application.controlLoopModule() {

    val controlLoopHandler by kodein().instance<ControlLoopHandler>()

    routing {
        post("/operator/controlLoop/registerAuditSink") {
            //log.info("--------------> Received Audit Event ")
            val request = call.receive<V1alpha1AuditSink>()
            controlLoopHandler.registerAuditSink(request)
            call.respond(HttpStatusCode.OK)
        }
        post("/operator/controlLoop/auditEvent") {
            val request = call.receive<Any>()
            controlLoopHandler.processAuditEvent(request)
            call.respond(HttpStatusCode.OK)
        }
    }
}

open class ControlLoopHandler(private val operatorApiClient: OperatorApiClient) {

    private val log = logger(ControlLoopHandler::class)

    suspend fun registerAuditSink(auditSink: V1alpha1AuditSink): V1alpha1AuditSink {
        return operatorApiClient.auditSinkApiClient().create(auditSink).`object`
    }

    suspend fun processAuditEvent(auditEvent: Any) {
        log.info("Received Audit Event : ${auditEvent}")
    }
}