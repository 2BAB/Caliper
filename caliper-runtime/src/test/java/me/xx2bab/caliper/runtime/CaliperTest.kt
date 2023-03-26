package me.xx2bab.caliper.runtime

import io.mockk.*
import org.junit.Test

class CaliperTest {

    @Test
    fun `The visitor will be added and invoked successfully`() {
        val className = "ClassName"
        val elementName = "elementName"
        val paramKeys = arrayOf("p1", "p2")
        val paramValues: Array<Any> = arrayOf("v1", "v2")

        val visitor = mockk<SignatureVisitor>()
        every { visitor.visit(className, elementName, paramKeys, paramValues) } just runs

        Caliper.register(visitor)
        Caliper.log(className, elementName, paramKeys, paramValues)

        verify { visitor.visit(className, elementName, paramKeys, paramValues) }
    }

}