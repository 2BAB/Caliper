package me.xx2bab.caliper.ksp

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CaliperMeta(val metadataInJSON: String)
