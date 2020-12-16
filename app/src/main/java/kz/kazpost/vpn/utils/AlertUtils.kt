package kz.kazpost.vpn.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(message: String?) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toast(message: ResourceString?) {
    toast(message?.format(this))
}