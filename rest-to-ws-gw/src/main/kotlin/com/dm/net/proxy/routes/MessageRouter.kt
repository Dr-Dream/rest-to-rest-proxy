package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.model.ProxyMessage
import com.dm.net.proxy.model.SubscribeMessage
import com.dm.net.proxy.routes.JacksonRouter.Companion.UNMARSHAL_ROUTE
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.component.vertx.websocket.VertxWebsocketConstants
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MessageRouter(context: CamelContext?, mapper: ObjectMapper) : RouteBuilder(context) {

    //val jacksonDataFormat: JacksonDataFormat = JacksonDataFormat(mapper, ProxyMessage::class.java)


    override fun configure() {

        from("seda:message-router")
            .routeId("message-router")
            .log("Message route \${body}")
            .to("log:?level=INFO&showAll=true")
            .choice()
                .`when`(body().isInstanceOf(SubscribeMessage::class.java)).log("Subscribe").to("seda:subscribe")
                .`when`(body().isInstanceOf(HttpRequestMessage::class.java)).log("Request").to("seda:request-dispatcher")
                .`when`(body().isInstanceOf(HttpResponseMessage::class.java))
                        .log("Response").to(CamelHttpRouteBuilder.CONTROLLER_OUTPUT)
                .otherwise().throwException(RuntimeException("Unknown message"))
//            .end()
//            .log("Choice finished")
//            .to("log:?level=INFO&showAll=true")
            //.end()
/*
        from("seda:to-client")
            .routeId("router-response")
            .to("log:?level=INFO&showAll=true")
            .marshal(jacksonDataFormat)
            .to("vertx-websocket://stream?exchangePattern=InOut")
*/
    }

}