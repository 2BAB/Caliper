package me.xx2bab.caliper.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.xx2bab.caliper.sample.library.LibrarySampleClass

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.navi_to_privacy).setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
        findViewById<Button>(R.id.navi_to_battery_optimization).setOnClickListener {
            startActivity(Intent(this, BatteryOptimizationActivity::class.java))
        }

    }

}