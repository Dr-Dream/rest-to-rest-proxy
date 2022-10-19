package com.dm.net.proxy.model

import com.dm.net.proxy.model.MessageConstants.SUBSCRIPTION_MESSAGE_TYPE


data class SubscriptionMessage(
    val endpoint: String,
    val clientSessionId: String
) : ProxyMessage {
    override val type = SUBSCRIPTION_MESSAGE_TYPE
}
