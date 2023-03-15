package me.xx2bab.caliper.sample

import android.content.Context
import android.widget.Button

data class TriggerButton(
    val text: String,
    val id: Int,
    val expectedResult: String,
    val onClickListener: () -> Unit
)

fun makeTriggerButton(context: Context, triggerButton: TriggerButton): Button {
    val button = Button(context)
    button.text = triggerButton.text
    button.id = triggerButton.id
    button.setOnClickListener {
        triggerButton.onClickListener()
    }
    return button
}