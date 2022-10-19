package com.dm.net.proxy.routes

import com.dm.net.proxy.routes.JacksonRouter.Companion.MARSHAL_ROUTE
import com.dm.net.proxy.routes.JacksonRouter.Companion.UNMARSHAL_ROUTE
import com.dm.net.proxy.routes.MessageRouter.Companion.MESSAGE_ROUTER
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import javax.enterprise.context.ApplicationScoped


@ApplicationScoped
class CamelHttpRouteBuilder(context: CamelContext?) : RouteBuilder(context) {

    companion object {
        const val VERTX_CONTROLLER="http-controller"
        const val VERTX_RESPONSES="http-response"
        private const val CONTROLLER_INPUT_VERTX="vertx:$VERTX_CONTROLLER?exchangePattern=InOnly&pubSub=true"
        private const val CONTROLLER_OUTPUT_VERTX="vertx:$VERTX_RESPONSES?exchangePattern=InOnly&pubSub=true"
        const val CONTROLLER_OUTPUT="seda:controller-output?exchangePattern=InOnly"
    }

    override fun configure() {

        from(CONTROLLER_INPUT_VERTX)
            .routeId("controller")
            .log("Controller request.")
            .to(UNMARSHAL_ROUTE)
            .to(MESSAGE_ROUTER)

        from(CONTROLLER_OUTPUT)
            .routeId("controller-reply")
            .log("Reply to controller")
            .to(MARSHAL_ROUTE)
            .to(CONTROLLER_OUTPUT_VERTX)


    }


}