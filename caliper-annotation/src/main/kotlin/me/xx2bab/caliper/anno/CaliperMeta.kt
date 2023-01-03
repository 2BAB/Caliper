package me.xx2bab.caliper.anno

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CaliperMeta(val metadataInJSON: String)
