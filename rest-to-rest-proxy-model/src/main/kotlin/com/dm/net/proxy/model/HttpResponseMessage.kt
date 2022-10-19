package com.dm.net.proxy.model

import com.dm.net.proxy.model.MessageConstants.HTTP_RESPONSE_MESSAGE_TYPE

data class HttpResponseMessage(
    override val requestId: String,
    override val endpoint: String,
    override val body: String?,
    val statusCode: Int,
    override val headers: Map<String, String?> = mutableMapOf(),
    val statusMessage: String?
) : HttpProxyMessage {
    override val type = HTTP_RESPONSE_MESSAGE_TYPE

    companion object {
        fun notFoundMessage(requestId: String, endpoint: String, message: String) = HttpResponseMessage(
            requestId = requestId,
            endpoint = endpoint,
            body = message,
            statusCode = 404,
            statusMessage = message
        )
        fun exceptionMessage(requestId: String, endpoint: String, message: String?)= HttpResponseMessage(
            requestId = requestId,
            endpoint = endpoint,
            body = message,
            statusCode = 500,
            statusMessage = message
        )
    }
}
