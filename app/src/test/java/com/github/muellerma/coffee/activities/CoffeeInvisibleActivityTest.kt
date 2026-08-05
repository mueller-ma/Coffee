package com.github.muellerma.coffee.activities

import android.content.Context
import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class CoffeeInvisibleActivityTest {
    private val context: Context = RuntimeEnvironment.getApplication()

    @Test
    fun toggleIntent_targetsCoffeeInvisibleActivity() {
        val intent = CoffeeInvisibleActivity.toggleIntent(context)

        assertEquals(CoffeeInvisibleActivity::class.java.name, intent.component?.className)
        assertEquals(CoffeeInvisibleActivity.ACTION_TOGGLE, intent.action)
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
