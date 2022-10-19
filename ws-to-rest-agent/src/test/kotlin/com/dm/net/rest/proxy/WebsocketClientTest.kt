package com.dm.net.rest.proxy

import com.dm.net.rest.proxy.ws.WebsocketClient
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.URI
import javax.websocket.ContainerProvider


@QuarkusTest
internal class WebsocketClientTest {

    @TestHTTPResource("8.8.8.8:9090/stream/")
    var uri: URI? = null


    @Test
    fun `test_ws_is_up`() {

        val s = ContainerProvider.getWebSocketContainer().connectToServer(WebsocketClient::class.java, uri)

        s.asyncRemote.sendObject("123")

    }

}