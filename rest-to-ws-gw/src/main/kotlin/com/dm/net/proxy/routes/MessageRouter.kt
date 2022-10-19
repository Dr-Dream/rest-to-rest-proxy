package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.model.SubscribeMessage
import com.dm.net.proxy.routes.SubscriptionRouter.Companion.SUBSCRIBE
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class MessageRouter(context: CamelContext?) : RouteBuilder(context) {

    companion object {
        const val MESSAGE_ROUTER = "seda:message-router"
    }

    override fun configure() {

        from(MESSAGE_ROUTER)
            .routeId("message-router")
            .log("Message route \${body}")
            .choice()
                .`when`(body().isInstanceOf(SubscribeMessage::class.java)).log("Subscribe").to(SUBSCRIBE)
                .`when`(body().isInstanceOf(HttpRequestMessage::class.java)).log("Request").to("seda:request-dispatcher")
                .`when`(body().isInstanceOf(HttpResponseMessage::class.java))
                        .log("Response").to(CamelHttpRouteBuilder.CONTROLLER_OUTPUT)
                .otherwise().throwException(RuntimeException("Unknown message"))

    }

}