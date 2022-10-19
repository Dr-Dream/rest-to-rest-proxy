package com.dm.net.proxy.controller

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.routes.CamelHttpRouteBuilder.Companion.VERTX_CONTROLLER
import com.dm.net.proxy.routes.CamelHttpRouteBuilder.Companion.VERTX_RESPONSES
import com.dm.net.rest.proxy.logging.logger
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.vertx.web.Body
import io.quarkus.vertx.web.Param
import io.quarkus.vertx.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.mutiny.core.eventbus.EventBus
import io.vertx.mutiny.core.http.HttpServerRequest
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class RestController(
    val eventBus: EventBus,
    val mapper: ObjectMapper,
    @ConfigProperty(name="gateway.timeout")
    val timeout:Duration
) {

    companion object {
        val log = logger()
    }

    @Route(path = "/request/:endpoint/**")
    fun httpRequest(@Param endpoint: String, @Body body: String?, request: HttpServerRequest, context: RoutingContext) {
        val headers = request.headers()
        val message = HttpRequestMessage(
            request.method().name(),
            endpoint,
            request.path().replaceFirst("/request/$endpoint", ""),
            request.query(),
            request.headers().associate { mutableEntry -> Pair(mutableEntry.key, headers.get(mutableEntry.key)) },
            body
        )


        log.info("Incoming request {}", message)
        eventBus.publish(VERTX_CONTROLLER, mapper.writeValueAsString(message))
        eventBus.localConsumer<String>(VERTX_RESPONSES).bodyStream().toMulti()
            .onItem().transform { m -> mapper.readValue(m, HttpResponseMessage::class.java) }
            .select().where { m -> message.requestId == m.requestId && m is HttpResponseMessage }
            .collect().first()
            .ifNoItem().after(timeout).recoverWithItem(
                HttpResponseMessage.exceptionMessage(
                    requestId = message.requestId,
                    endpoint = message.endpoint,
                    message = "Timeout waiting response"
                )
            )
            .onFailure().recoverWithItem { exception ->
                HttpResponseMessage.exceptionMessage(
                    requestId = message.requestId,
                    endpoint = message.endpoint,
                    message = exception.message
                )
            }
            .subscribe().with { response ->
                log.info("Sending response.")
                response.headers.forEach { (key, value) -> context.response().headers().add(key, value) }
                context.response().setStatusCode(response.statusCode).setStatusMessage(response.statusMessage)
                    .send(response.body ?: "")
            }
    }
}