package me.xx2bab.caliper.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import me.xx2bab.caliper.sample.library.LibrarySampleClass

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(
            this,
            LibrarySampleClass().commonMethodReturnsString(),
            Toast.LENGTH_LONG
        ).show()
    }

}