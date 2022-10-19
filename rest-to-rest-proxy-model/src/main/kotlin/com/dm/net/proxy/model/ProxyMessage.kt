package com.dm.net.proxy.model

import com.dm.net.proxy.model.MessageConstants.HTTP_REQUEST_MESSAGE_TYPE
import com.dm.net.proxy.model.MessageConstants.HTTP_RESPONSE_MESSAGE_TYPE
import com.dm.net.proxy.model.MessageConstants.SUBSCRIBE_MESSAGE_TYPE
import com.dm.net.proxy.model.MessageConstants.SUBSCRIPTION_MESSAGE_TYPE
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(SubscribeMessage::class, name = SUBSCRIBE_MESSAGE_TYPE),
    JsonSubTypes.Type(SubscriptionMessage::class, name = SUBSCRIPTION_MESSAGE_TYPE),
    JsonSubTypes.Type(HttpRequestMessage::class, name = HTTP_REQUEST_MESSAGE_TYPE),
    JsonSubTypes.Type(HttpResponseMessage::class, name = HTTP_RESPONSE_MESSAGE_TYPE),
)
interface ProxyMessage {
    val type: String
}