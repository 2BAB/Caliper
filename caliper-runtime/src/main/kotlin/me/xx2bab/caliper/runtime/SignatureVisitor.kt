package me.xx2bab.caliper.runtime

interface SignatureVisitor {

    fun visit(
        className: String,
        elementName: String, // Method or Field name
        parameterNames: Array<String>,
        parameterValues: Array<Any>
    )

}