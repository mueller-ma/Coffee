package com.github.muellerma.coffee

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(@StringRes msg: Int) {
    Toast
        .makeText(this, msg, Toast.LENGTH_SHORT)
        .show()
}