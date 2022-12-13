package com.dm.net.rest.proxy.web

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.HttpResponseMessage
import com.dm.net.rest.proxy.logging.logger
import io.smallrye.mutiny.Uni
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.WebClient
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.UriBuilder

@ApplicationScoped
class ProxyWebClient(
    vertx: Vertx,
    @ConfigProperty(name = "client.uri")
    val uri: String,
    @ConfigProperty(name = "client.timeout")
    val timeout: Duration
) {

    companion object {
        private val log = logger()
    }

    private val webClientOptions = WebClientOptions()
        .setVerifyHost(false)
        .setTrustAll(true)

    private val client: WebClient = WebClient.create(vertx,webClientOptions)

    fun buildUri(requestMessage: HttpRequestMessage): String {
        val builder = UriBuilder.fromUri(uri)
            .path(requestMessage.path)
        if (requestMessage.query != null) builder.replaceQuery(requestMessage.query)
        return builder.build().toString()

    }

    fun doRequest(requestMessage: HttpRequestMessage): Uni<HttpResponseMessage> {
        val httpRequest = client.getAbs(buildUri(requestMessage))
            .method(HttpMethod.valueOf(requestMessage.method))



        httpRequest
            .headers()
            .add("X-WSProxy-For-Id", requestMessage.requestId)

        requestMessage.headers
            .filter { "Host"!=it.key }
            .forEach { entry -> httpRequest.putHeader(entry.key, entry.value as String) }

        val body: String = requestMessage.body ?: ""

        log.info(
            "{} Starting '{}' request on {}:{}{}",
            requestMessage.requestId,
            httpRequest.method(),
            httpRequest.host(),
            httpRequest.port(),
            httpRequest.uri()
        )
        return httpRequest.sendBuffer(Buffer.buffer(body))

            .onItem().invoke { response ->
                log.info(
                    "Response received: {} {} headers:{} body:{}",
                    response.statusCode(),
                    response.statusMessage(),
                    response.headers(),
                    response.body()
                )
            }
            .ifNoItem().after(timeout).fail()
            .onItem().transform { response ->
                HttpResponseMessage(
                    requestId = requestMessage.requestId,
                    headers = response.headers()
                        .associate { mutableEntry -> Pair(mutableEntry.key, response.headers().get(mutableEntry.key)) },
                    body = response.bodyAsString(),
                    endpoint = requestMessage.endpoint,
                    statusCode = response.statusCode(),
                    statusMessage = response.statusMessage()
                )
            }
            .onFailure().recoverWithItem { exc ->
                HttpResponseMessage.exceptionMessage(
                    requestId = requestMessage.requestId,
                    endpoint = requestMessage.endpoint,
                    message = exc.message

                )
            }


    }
}