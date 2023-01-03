package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod

@Serializable
data class ProxiedMetaData(
    @Required val proxiedMethods: MutableList<ProxiedMethod> = mutableListOf(),
    @Required val proxiedFields: MutableList<ProxiedField> = mutableListOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf() // Used in annotation processor only
)


