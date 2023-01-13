package me.xx2bab.caliper.runtime.record

import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CaliperLoggerTest {

    private val androidLogger = spyk(DefaultAndroidLogger())

    @Test
    fun abc() {
        val logger = CaliperLogger(androidLogger)
        logger.visit(
            "className",
            "methodName",
            arrayOf("p1", "p2"),
            arrayOf(1, arrayOf("foo", "bar"))
        )
        verify { androidLogger.d(MAIN_TAG,
            "Proxy triggered: class \"className\" with element \"methodName\" :\n" +
                "    --> process identifier: me.xx2bab.caliper.runtime.test\n" +
                "    --> thread: main thread\n" +
                "    --> parameters: p1 = 1, p2 = [\"foo\", \"bar\"], ") }
    }

}