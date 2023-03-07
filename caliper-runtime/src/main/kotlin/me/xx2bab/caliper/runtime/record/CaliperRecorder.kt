package me.xx2bab.caliper.runtime.record

import me.xx2bab.caliper.runtime.SignatureVisitor

class CaliperRecorder: SignatureVisitor {

    override fun visit(
        className: String,
        elementName: String,
        parameterNames: Array<String>,
        parameterValues: Array<Any>
    ) {
        TODO("Not yet implemented")
    }

}