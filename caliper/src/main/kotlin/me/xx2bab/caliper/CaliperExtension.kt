package me.xx2bab.caliper

import com.android.build.api.variant.Variant
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class CaliperExtension @Inject constructor(
    objects: ObjectFactory
) {

    var kotlinEnableByVariant: EnableByVariant? = null

    var groovyEnableByVariant: Closure<Boolean>? = null

    // For Gradle Kotlin DSL
    fun enableByVariant(selector: EnableByVariant) {
        kotlinEnableByVariant = selector
    }

    // For Gradle Groovy DSL
    fun enableByVariant(selector: Closure<Boolean>) {
        groovyEnableByVariant = selector.dehydrate()
    }


    companion object {

        internal fun isFeatureEnabled(
            variant: Variant,
            kotlinEnableByVariant: EnableByVariant?,
            groovyEnableByVariant: Closure<Boolean>?
        ): Boolean = when {
            kotlinEnableByVariant != null -> {
                kotlinEnableByVariant.invoke(variant)
            }

            groovyEnableByVariant != null -> {
                groovyEnableByVariant.call(variant)
            }

            else -> false
        }

    }
}

internal typealias EnableByVariant = (variant: Variant) -> Boolean

