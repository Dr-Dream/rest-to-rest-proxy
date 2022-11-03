package com.dm.net.rest.proxy.config

import com.dm.net.proxy.model.*
import io.quarkus.runtime.annotations.RegisterForReflection

@RegisterForReflection(
    targets = [
        SubscribeMessage::class,
        HttpRequestMessage::class,
        HttpResponseMessage::class,
        ProxyMessage::class,
        SubscriptionMessage::class
    ]
)
class ModelReflectionsConfig