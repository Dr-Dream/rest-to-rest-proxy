package com.dm.net.proxy.model

import com.dm.net.proxy.model.MessageConstants.SUBSCRIBE_MESSAGE_TYPE

data class SubscribeMessage(
    val endpoint: String
) : ProxyMessage {
    override val type = SUBSCRIBE_MESSAGE_TYPE
}
