package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.model.ProxyMessage
import com.dm.net.proxy.routes.JacksonRouter.Companion.MARSHAL_ROUTE
import com.dm.net.proxy.routes.JacksonRouter.Companion.UNMARSHAL_ROUTE
import com.dm.net.rest.proxy.logging.logger
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.camel.AggregationStrategy
import org.apache.camel.CamelContext
import org.apache.camel.Exchange
import org.apache.camel.ExchangePattern
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ServerWebsocketRoute(context: CamelContext?, mapper: ObjectMapper) : RouteBuilder(context) {


    companion object {
        val log = logger()
    }


    val aggregationStrategy: AggregationStrategy = AggregationStrategy { oldExchange, newExchange ->
        log.info("Aggregate")
        log.info("Old {}", oldExchange)
        log.info("New {}", newExchange)
        if (oldExchange == null) {
            // we start a new correlation group, so complete all previous groups
            //newExchange.setProperty(Exchange.AGGREGATION_COMPLETE_ALL_GROUPS, true);
            return@AggregationStrategy newExchange;
        }
        if ((newExchange != null) && (newExchange.`in`.body is HttpResponseMessage)) {
            oldExchange.`in`.body = newExchange.`in`.body
        }
        return@AggregationStrategy oldExchange
    }

    override fun configure() {
        from("vertx-websocket://stream")
            .routeId("websocket")
            .log("Websocket message")
            .to(UNMARSHAL_ROUTE)
            .to("seda:message-router")

        from("seda:responses")
            .routeId("responses")
            .log("downstream")
            .choice()
                .`when`(body().isInstanceOf(HttpRequestMessage::class.java))
                    .log("Send to client")
                    .process { exchange ->
                        exchange.`in`.headers["X-ProxyId"] =
                            exchange.`in`.getBody(HttpRequestMessage::class.java).requestId
                    }
                    .to(MARSHAL_ROUTE)
                    .to("vertx-websocket://stream?exchangePattern=InOnly")
                    .to(UNMARSHAL_ROUTE)
                    .to("seda:aggregate")
                .`when`(body().isInstanceOf(HttpResponseMessage::class.java))
                    .process { exchange ->
                        exchange.`in`.headers["X-ProxyId"] =
                            exchange.`in`.getBody(HttpResponseMessage::class.java).requestId
                    }
                    .to("seda:aggregate")
                .otherwise().throwException(RuntimeException("Kaka"))


        from("seda:aggregate")
            //.setExchangePattern(ExchangePattern.InOptionalOut)
            .routeId("aggregate")
            .log("Before aggregate")
            .to("log:?level=INFO&showAll=true")
//            .aggregate(header("X-ProxyId"))
            .aggregate(constant(true))
            .id("request-response-aggregate")
            .aggregationStrategy(aggregationStrategy)
            .completionSize(constant(2))
            .completionTimeout(3000000L)
            // wait for 0.5 seconds to aggregate
            .log("Reply from downstream")
            .to("log:?level=INFO&showAll=true")
            //.end()

        //.to("vertx:http-controller")
        //.to

    }
}