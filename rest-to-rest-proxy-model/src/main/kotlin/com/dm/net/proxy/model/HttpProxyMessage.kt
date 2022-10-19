package com.dm.net.proxy.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(HttpRequestMessage::class, name = MessageConstants.HTTP_REQUEST_MESSAGE_TYPE),
    JsonSubTypes.Type(HttpResponseMessage::class, name = MessageConstants.HTTP_RESPONSE_MESSAGE_TYPE),
)
interface HttpProxyMessage:ProxyMessage {
    val requestId: String
    val endpoint: String
    val headers: Map<String,String?>
    val body: String?
}