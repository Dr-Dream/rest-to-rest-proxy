package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.routes.JacksonRouter.Companion.MARSHAL_ROUTE
import com.dm.net.proxy.routes.JacksonRouter.Companion.UNMARSHAL_ROUTE
import com.dm.net.proxy.routes.SubscriptionRouter.Companion.UNSUBSCRIBE
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.vertx.websocket.VertxWebsocketConstants
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ServerWebsocketRoute(context: CamelContext?) : RouteBuilder(context) {

    companion object {
        const val AGENT_DOWNSTREAM="seda:responses"
        private const val VERTX_WS_INPUT="vertx-websocket://stream?bridgeErrorHandler=true"
        private const val VERTX_WS_OUTPUT="vertx-websocket://stream?exchangePattern=InOnly"
    }

    override fun configure() {

        errorHandler(
            deadLetterChannel("seda:ws-error")
        )

        from("seda:ws-error")
            .setBody(header(VertxWebsocketConstants.CONNECTION_KEY))
            .log("Error \${body}: \${exception}")
            .wireTap(UNSUBSCRIBE)
            .end()


        from(VERTX_WS_INPUT)
            .routeId("websocket")
            .log("Websocket message")
            .to(UNMARSHAL_ROUTE)
            .to("seda:message-router")

        from(AGENT_DOWNSTREAM)
            .routeId("responses")
            .process { exchange ->
                exchange.`in`.headers["X-ProxyId"] =
                    exchange.`in`.getBody(HttpRequestMessage::class.java).requestId
            }
            .log("Send to agent.")
            .to(MARSHAL_ROUTE)
            .to(VERTX_WS_OUTPUT)
    }
}