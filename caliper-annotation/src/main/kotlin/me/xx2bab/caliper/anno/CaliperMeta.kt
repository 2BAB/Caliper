package me.xx2bab.caliper.anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class CaliperMeta(val metadataInJSON: String)
