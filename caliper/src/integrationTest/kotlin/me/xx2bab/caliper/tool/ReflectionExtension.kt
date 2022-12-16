package me.xx2bab.caliper.proxy

fun Any.invokeMethod(clazz: Class<*>, methodName: String, vararg args: String): String {
    val method = clazz.getDeclaredMethod(methodName)
    method.isAccessible = true
    return if (args.isEmpty()) {
        method.invoke(this).toString()
    } else {
        method.invoke(this, args).toString()
    }
}

fun Any.getFieldValueInString(clazz: Class<*>, fieldName: String): String {
    val field = clazz.getDeclaredField(fieldName)
    field.isAccessible = true
    return field.get(this).toString()
}