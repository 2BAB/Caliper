package me.xx2bab.caliper.core

import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod

// sync from ProxiedMetaData
data class ProxyConfig(
    val proxiedMethods: MutableList<ProxiedMethod> = mutableListOf(),
    val proxiedFields: MutableList<ProxiedField> = mutableListOf(),
)