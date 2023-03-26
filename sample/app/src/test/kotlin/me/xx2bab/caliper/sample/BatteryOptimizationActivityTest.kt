package me.xx2bab.caliper.sample

import android.widget.Button
import android.widget.TextView
import me.xx2bab.caliper.runtime.Caliper
import me.xx2bab.caliper.runtime.SignatureVisitor
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BatteryOptimizationActivityTest {

    @Test
    fun `BatteryOptimizationActivity all buttons should invoke through Caliper proxy`() {
        Robolectric.buildActivity(BatteryOptimizationActivity::class.java).use { controller ->
            controller.setup() // Moves Activity to RESUMED state
            val activity: BatteryOptimizationActivity = controller.get()
            val outputTextView = activity.findViewById<TextView>(R.id.return_content)
            var logReplica = ""
            Caliper.register(object: SignatureVisitor {
                override fun visit(
                    className: String,
                    elementName: String,
                    parameterNames: Array<String>,
                    parameterValues: Array<Any>
                ) {
                    logReplica = "$className->$elementName"
                }
            })
            activity.triggerButtons.forEach {
                val but = activity.findViewById<Button>(it.id)
                but.performClick()
                assertThat(logReplica, `is`(it.expectedResult))
            }
        }
    }

}