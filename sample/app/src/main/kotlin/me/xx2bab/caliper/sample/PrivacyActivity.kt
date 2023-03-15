package me.xx2bab.caliper.sample

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi

class PrivacyActivity : AppCompatActivity() {

    lateinit var outputTv: TextView
    lateinit var expectedTv: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    lateinit var triggerButtons: List<TriggerButton>

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100) {
            outputTv.text = "true"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        triggerButtons = listOf(
            TriggerButton(
                text = "getSerial()",
                id = 10001,
                expectedResult = "android/os/Build->getSerial",
                onClickListener = {
                    outputTv.text = Build.getSerial()
                    expectedTv.text = ""
                }
            ),
            TriggerButton(
                text = "SERIAL",
                id = 10002,
                expectedResult = "android/os/Build->getSerialField",
                onClickListener = {
                    outputTv.text = Build.SERIAL
                }
            ),
            TriggerButton(
                text = "getString()",
                id = 10003,
                expectedResult = "android/provider/Settings\$Settings.Secure->getString",
                onClickListener = {
                    outputTv.text = Settings.Secure.getString(this.application.contentResolver,
                        Settings.Secure.ANDROID_ID)
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
        expectedTv = findViewById(R.id.expect_content)
        val buttonList = findViewById<LinearLayout>(R.id.button_list)

        triggerButtons.forEach {
            buttonList.addView(makeTriggerButton(this, it))
        }

    }
}