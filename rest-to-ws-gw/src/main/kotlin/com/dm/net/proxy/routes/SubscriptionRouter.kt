package com.dm.net.proxy.routes

import com.dm.net.proxy.model.SubscribeMessage
import com.dm.net.proxy.processor.SubscriptionProcessor
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.vertx.websocket.VertxWebsocketConstants
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SubscriptionRouter(context: CamelContext?, val processor: SubscriptionProcessor) : RouteBuilder(context) {
    companion object {
        const val SUBSCRIBE = "seda:subscribe"
        const val UNSUBSCRIBE = "seda:unsubscribe"
    }
    override fun configure() {
        from(UNSUBSCRIBE)
            .errorHandler(noErrorHandler())
            .log("Unsubscribing client. \${body}")
            .process { exchange -> processor.unsubscribe(exchange.`in`.body) }
            .setBody(constant("OK"))




        from(SUBSCRIBE)
            .routeId("subscription-route")
            .log("New client subscription.")
            .process { exchange ->
                    exchange.`in`.body = processor.subscribe(
                        exchange.`in`.getBody(SubscribeMessage::class.java),
                        exchange.`in`.getHeader(VertxWebsocketConstants.CONNECTION_KEY,String::class.java)
                    )
            }
    }


}