package me.xx2bab.caliper.core

import me.xx2bab.caliper.common.*

// sync from ProxiedMetaData
data class ProxyConfig(
    val proxiedMethods: MutableList<ProxiedMethod> = mutableListOf(),
    val proxiedFields: MutableList<ProxiedField> = mutableListOf(),
    val proxiedClasses: MutableList<ProxiedClass> = mutableListOf(),
)