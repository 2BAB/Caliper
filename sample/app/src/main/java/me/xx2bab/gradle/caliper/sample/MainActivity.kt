package me.xx2bab.gradle.caliper.sample

import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val serial = Build.SERIAL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun getAndroidId(): String {
        return Settings.Secure.getString(contentResolver, "android_id")
    }

    private fun getAndroidIdProxy(): String {
        return Caliper.getString(contentResolver, "android_id")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAndroidSerial(): String {
        return Build.getSerial()
    }

    private fun getAndroidSerialProxy(): String {
        return Caliper.getSerial()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAndroidSerialField(): String {
        return Build.SERIAL
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAndroidSerialFieldProxy(): String {
        return Caliper.SERIAL
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAndroidBrandField(): String {
        return Build.BRAND
    }
}

