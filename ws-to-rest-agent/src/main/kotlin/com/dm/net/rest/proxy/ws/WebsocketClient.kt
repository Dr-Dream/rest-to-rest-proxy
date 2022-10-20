package com.dm.net.rest.proxy.ws

import com.dm.net.proxy.model.HttpRequestMessage
import com.dm.net.proxy.model.ProxyMessage
import com.dm.net.proxy.model.SubscribeMessage
import com.dm.net.rest.proxy.logging.logger
import com.dm.net.rest.proxy.web.ProxyWebClient
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.runtime.StartupEvent
import io.quarkus.vertx.ConsumeEvent
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.subscription.Cancellable
import io.vertx.mutiny.core.eventbus.EventBus
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URI
import java.time.Duration
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.websocket.*


@ApplicationScoped
@ClientEndpoint
class WebsocketClient(
    val bus: EventBus,
    val mapper: ObjectMapper,
    val client: ProxyWebClient,
    @ConfigProperty(name = "gateway.uri")
    val gatewayUri: String,
    @ConfigProperty(name = "gateway.endpoint")
    val gatewayEndpoint: String
) {
    var socket: Session? = null

    companion object {
        val log = logger()
    }


    @ConsumeEvent("keep-alive", blocking = true, ordered = true)
    fun consumeEvents(msg: String) {
        log.info("Message send {}", msg)
        if (socket == null || !socket!!.isOpen) {
            try {
                log.info("Connecting")
                socket = ContainerProvider.getWebSocketContainer()
                    .connectToServer(WebsocketClient::class.java, URI.create(gatewayUri))
                socket?.basicRemote?.sendObject(mapper.writeValueAsString(SubscribeMessage(gatewayEndpoint)))
            } catch (e: Exception) {
                log.warn("Exception during connect ", e)
            }

        } else if (socket!=null && socket!!.isOpen ){
            socket?.basicRemote?.sendObject(mapper.writeValueAsString(SubscribeMessage(gatewayEndpoint)))
        }
    }


    lateinit var keepAlive: Cancellable

    fun onStart(@Observes event: StartupEvent) {
        log.info("StartUp")
        keepAlive = Multi.createFrom().ticks().every(Duration.ofSeconds(30))
            .subscribe().with { tick ->
                bus.publish("keep-alive", "ping")
            }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        log.info("Shutdown")
    }

    @PostConstruct
    fun init() {
        log.info("PostConstruct")
        log.info("1")
    }

    @OnOpen
    fun onOpen(session: Session?) {
        log.info("(onOpen) {}", session)
    }

    @OnClose
    fun onClose(session: Session) {
        log.info("(onClose) {}", session)
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable) {
        log.error("(onError) {}", session, throwable)
    }

    @OnMessage
    fun onMessage(message: String, session: Session) {
        log.info("(onMessage) {}", message)
        try {
            val request = mapper.readValue(message, ProxyMessage::class.java)
            if (request is HttpRequestMessage) {
                client.doRequest(request)
                    //.onItem().delayIt().by(Duration.ofSeconds(30))
                    .subscribe()
                    .with { item ->
                        log.info(
                            "Response sent {}",
                            item
                        );session.basicRemote.sendObject(mapper.writeValueAsString(item))
                    }

            }
        } catch (e: Throwable) {
            log.error("Unable to handle message", e)
        }
    }
}