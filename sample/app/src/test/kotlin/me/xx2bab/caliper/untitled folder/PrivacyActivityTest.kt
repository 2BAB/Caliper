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
class PrivacyActivityTest {

    @Test
    fun `PrivacyActivity all buttons should invoke through Caliper proxy`() {
        Robolectric.buildActivity(PrivacyActivity::class.java).use { controller ->
            controller.setup() // Moves Activity to RESUMED state
            val activity: PrivacyActivity = controller.get()
            val outputTextView = activity.findViewById<TextView>(R.id.return_content)
            val logTextView = activity.findViewById<TextView>(R.id.log_content)

            Caliper.accept(object: SignatureVisitor {
                override fun visit(
                    className: String,
                    elementName: String,
                    parameterNames: Array<String>,
                    parameterValues: Array<Any>
                ) {
                    logTextView.text = "$className->$elementName"
                }
            })

            activity.triggerButtons.forEach {
                activity.findViewById<Button>(it.id).performClick()
                 assertThat(logTextView.text, `is`(it.expectedResult))
            }
        }
    }

}