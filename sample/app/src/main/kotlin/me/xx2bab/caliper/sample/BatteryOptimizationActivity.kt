package me.xx2bab.caliper.sample

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import me.xx2bab.caliper.runtime.Caliper
import me.xx2bab.caliper.runtime.SignatureVisitor

class BatteryOptimizationActivity : AppCompatActivity() {
    lateinit var outputTv: TextView
    lateinit var logTv: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    lateinit var triggerButtons: List<TriggerButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_optimization)

        triggerButtons = listOf(
            TriggerButton(
                text = "Thread Replace",
                id = 10001,
                expectedResult = "NamedThread->constructor",
                onClickListener = {
                    val thread = Thread(Runnable {
                        Thread.sleep(1000)
                    })
                    thread.start()
                }
            ),
            TriggerButton(
                text = "WakeLock acquire/release",
                id = 10002,
                expectedResult = "android/os/PowerManager\$WakeLock->release",
                onClickListener = {
                    val wakeLock: PowerManager.WakeLock =
                        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
                        }
                    wakeLock.acquire()
                    wakeLock.release()
                }
            ),
        )

        outputTv = findViewById(R.id.return_content)
        logTv = findViewById(R.id.log_content)
        val buttonList = findViewById<LinearLayout>(R.id.button_list)

        triggerButtons.forEach {
            buttonList.addView(makeTriggerButton(this, it))
        }

        Caliper.register(object: SignatureVisitor {
            override fun visit(
                className: String,
                elementName: String,
                parameterNames: Array<String>,
                parameterValues: Array<Any>
            ) {
                logTv.text = "$className->$elementName"
            }
        })
    }
}