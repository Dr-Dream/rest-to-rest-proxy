package com.dm.net.proxy.model

import com.dm.net.proxy.model.MessageConstants.HTTP_REQUEST_MESSAGE_TYPE
import java.util.*

data class HttpRequestMessage(
    val method: String,
    override val endpoint: String,
    val path: String?,
    val query: String?,
    override val headers: Map<String,String?>,
    override val body: String?
): HttpProxyMessage {
    override val type=HTTP_REQUEST_MESSAGE_TYPE
    override val requestId= UUID.randomUUID().toString()
}
