package com.dm.net.proxy.routes

import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.proxy.model.ProxyMessage
import com.dm.net.proxy.routes.JacksonRouter.Companion.MARSHAL_ROUTE
import com.dm.net.proxy.routes.JacksonRouter.Companion.UNMARSHAL_ROUTE
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.runtime.annotations.RegisterForReflection
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import org.apache.camel.AggregationStrategy
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.component.vertx.VertxComponent
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class CamelHttpRouteBuilder(context: CamelContext?,mapper: ObjectMapper) : RouteBuilder(context) {

    companion object {
        const val VERTX_CONTROLLER="http-controller"
        const val VERTX_RESPONSES="http-response"
        private const val CONTROLLER_INPUT_VERTX="vertx:$VERTX_CONTROLLER?exchangePattern=InOnly"
        private const val CONTROLLER_OUTPUT_VERTX="vertx:$VERTX_RESPONSES?exchangePattern=InOnly"
        const val CONTROLLER_OUTPUT="seda:controller-output?exchangePattern=InOnly"
    }

    override fun configure() {
//        from("vertx:http-controller?exchangePattern=InOnly")
//            .to("seda:agg")
//            .setBody(constant("request"))
//            //.enrich("seda:proc")
//
//
//        from("seda:agg")
//            .aggregate(constant(true),aggregationStrategy)
//            .completionSize(1)
//            .completionTimeout(100000L)
//            .to("seda:proc")
//            .end()
//
//        from("seda:proc")
//            .log("<OUT> \${body}")
//            .to("vertx:http-response?exchangePattern=InOnly")

        from(CONTROLLER_INPUT_VERTX)
            .routeId("controller")
            .log("Controller request.")
            .to("log:?level=INFO&showAll=true")
            .to(UNMARSHAL_ROUTE)
            .to("seda:message-router")

        from(CONTROLLER_OUTPUT)
            .log("Reply to controller")
            .to("log:?level=INFO&showAll=true")
            .to(MARSHAL_ROUTE)
            .to(CONTROLLER_OUTPUT_VERTX)


    }


}