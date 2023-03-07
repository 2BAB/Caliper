package me.xx2bab.caliper.common

fun String.toCaliperWrapperSimpleName() = this + "_CaliperWrapper"
fun String.toCaliperWrapperFullNameBySlash() = Constants.CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH + "/" +this.toCaliperWrapperSimpleName()
