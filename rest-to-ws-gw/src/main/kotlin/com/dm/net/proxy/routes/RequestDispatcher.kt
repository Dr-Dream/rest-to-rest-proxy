package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.processor.SubscriptionProcessor
import com.dm.net.proxy.routes.CamelHttpRouteBuilder.Companion.CONTROLLER_OUTPUT
import com.dm.net.proxy.routes.ServerWebsocketRoute.Companion.AGENT_DOWNSTREAM
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.vertx.websocket.VertxWebsocketConstants
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class RequestDispatcher(context: CamelContext?, val subscriptions: SubscriptionProcessor) : RouteBuilder(context) {
    companion object {
        const val REQUEST_DISPATCHER = "seda:request-dispatcher"
    }
    override fun configure() {
        from(REQUEST_DISPATCHER)
            .routeId("request-dispatcher")
            .log("Request dispatch.")
            .choice().`when`(body().isInstanceOf(HttpRequestMessage::class.java)).to("seda:agent-call")
            .otherwise().setBody(constant("Unknown Message type"))

        from("seda:agent-call")
            .routeId("agent-call")
            .log("Agent call")
            .process { exchange ->
                exchange.`in`.headers[VertxWebsocketConstants.CONNECTION_KEY] =
                    subscriptions.getClientByEndpoint(exchange.`in`.getBody(HttpRequestMessage::class.java).endpoint)
            }
            .choice()
                .`when`(header(VertxWebsocketConstants.CONNECTION_KEY).isNull)
                    .process { exchange ->
                        exchange.`in`.body = HttpResponseMessage.notFoundMessage(
                            exchange.`in`.getBody(HttpRequestMessage::class.java).requestId,
                            exchange.`in`.getBody(HttpRequestMessage::class.java).endpoint,
                            "No agent found."
                        )
                    }
                    .to(CONTROLLER_OUTPUT)
            .otherwise()
                .to(AGENT_DOWNSTREAM)
    }
}