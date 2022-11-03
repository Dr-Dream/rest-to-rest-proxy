package com.dm.net.proxy.processor

import com.dm.net.proxy.model.SubscribeMessage
import com.dm.net.proxy.model.SubscriptionMessage
import com.dm.net.rest.proxy.logging.logger
import java.util.concurrent.ConcurrentHashMap
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SubscriptionProcessor {
    companion object {
        val log=logger()
    }


    val registry: MutableMap<String,String> = ConcurrentHashMap()


    fun subscribe(subscribe: SubscribeMessage,connectionKey:String): SubscriptionMessage{
        log.info("Incoming subscription '{}' (Connection key: '{}')",subscribe.endpoint,connectionKey)
        registry[subscribe.endpoint] = connectionKey
        return SubscriptionMessage(
            subscribe.endpoint,
            connectionKey
        )
    }

    fun getClientByEndpoint(endpoint: String): String? {
        return registry[endpoint]
    }

    fun unsubscribe(clientKey: Any?) {
        if( clientKey is String ) {
            log.info("Unregistering {}",clientKey)
            registry.entries.removeIf { it.value == clientKey }
        }
    }
}