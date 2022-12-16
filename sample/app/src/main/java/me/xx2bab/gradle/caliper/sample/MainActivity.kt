package me.xx2bab.gradle.caliper.sample

import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    val a = Caliper.getString(contentResolver, "android_id")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun getAndroidId(): String {
        return Settings.Secure.getString(contentResolver, "android_id")
    }

    private fun getAndroidId2(): String {
        return Caliper.getString(contentResolver, "android_id")
    }

}

