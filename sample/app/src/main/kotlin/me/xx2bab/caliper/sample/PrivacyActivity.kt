package me.xx2bab.caliper.sample

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import me.xx2bab.caliper.runtime.Caliper
import me.xx2bab.caliper.runtime.SignatureVisitor

class PrivacyActivity : AppCompatActivity() {

    lateinit var outputTv: TextView
    lateinit var logTv: TextView

    lateinit var triggerButtonsBelow26: List<TriggerButton>
    lateinit var triggerButtonsAbove26: List<TriggerButton>

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            outputTv.text = "true"
        }
    }

    fun test() {
        requestPermissions(arrayOf("android.permission.READ_PHONE_STATE"), 100)
    }

//    fun test2() {
//        ActivityProxy.requestPermissions(this, arrayOf("android.permission.READ_PHONE_STATE"), 100)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        // < 26, Android O
        triggerButtonsBelow26 = listOf(
//            TriggerButton(
//                text = "getSerial()",
//                id = 10001,
//                expectedResult = "android/os/Build->getSerial",
//                onClickListener = {
//                    outputTv.text = Build.getSerial()
//                }
//            ),
            TriggerButton(
                text = "SERIAL",
                id = 10002,
                expectedResult = "android/os/Build->getSerialField",
                onClickListener = {
                    outputTv.text = Build.SERIAL
                }
            ))

        // >= 26, Android O
        triggerButtonsAbove26 = listOf(
            TriggerButton(
                text = "getString()",
                id = 10003,
                expectedResult = "android/provider/Settings\$Settings.Secure->getString",
                onClickListener = {
                    outputTv.text = Settings.Secure.getString(
                        this.application.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                }
            ),
            TriggerButton(
                text = "requestPermissions()",
                id = 10004,
                expectedResult = "android/app/Activity->requestPermissions",
                onClickListener = {
                    requestPermissions(arrayOf("android.permission.READ_PHONE_STATE"), 100)
                }
            )
        )

        outputTv = findViewById(R.id.return_content)
        logTv = findViewById(R.id.log_content)
        val buttonList = findViewById<LinearLayout>(R.id.button_list)

        Caliper.register(object : SignatureVisitor {
            override fun visit(
                className: String,
                elementName: String,
                parameterNames: Array<String>,
                parameterValues: Array<Any>
            ) {
                logTv.text = "$className->$elementName"
            }
        })
        triggerButtonsBelow26.forEach {
            buttonList.addView(makeTriggerButton(this, it))
        }
        triggerButtonsAbove26.forEach {
            buttonList.addView(makeTriggerButton(this, it))
        }

    }
}