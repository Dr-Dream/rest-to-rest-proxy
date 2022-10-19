package com.dm.net.proxy.routes

import com.dm.net.proxy.model.ProxyMessage
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.camel.CamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class JacksonRouter(context: CamelContext?,val mapper: ObjectMapper) : RouteBuilder(context) {
    companion object {
        const val MARSHAL_ROUTE="seda:jackson-marshal?exchangePattern=InOut"
        const val UNMARSHAL_ROUTE="seda:jackson-unmarshal?exchangePattern=InOut"
    }

    val jacksonDataFormat: JacksonDataFormat = JacksonDataFormat(mapper, ProxyMessage::class.java)

    override fun configure() {
        from(MARSHAL_ROUTE)
            .routeId("jackson-marshaller")
            .log("Marshal \${body}")
  //          .to("log:?level=INFO&showAll=true")
            .marshal(jacksonDataFormat)
            .setBody(body().convertToString())
//            .log("Outgoing body \${body}")

        from(UNMARSHAL_ROUTE)
            .routeId("jackson-unmarshaller")
            .setBody(body().convertToString())
//            .log("Incoming body \${body}")
            .log("Unmarshal \${body}")
            .unmarshal(jacksonDataFormat)
//            .to("log:?level=INFO&showAll=true")

    }
}