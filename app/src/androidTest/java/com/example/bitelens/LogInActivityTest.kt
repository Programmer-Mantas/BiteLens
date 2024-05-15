package com.example.bitelens

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LogInActivityTest {

    @Rule
    @JvmField
    var mActivityScenarioRule = IntentsTestRule(LogInActivity::class.java)

    @Test
    fun logInActivityTest() {
        // Input text into email field
        onView(withId(R.id.editTextEmailLogin)).perform(clearText(), typeText("test@gmail.com"), closeSoftKeyboard())
        // Verify the email has been entered
        onView(withId(R.id.editTextEmailLogin)).check(matches(withText("test@gmail.com")))

        // Input text into password field
        onView(withId(R.id.editTextPasswordLogin)).perform(clearText(), typeText("test123"), closeSoftKeyboard())
        // Verify the password has been entered
        onView(withId(R.id.editTextPasswordLogin)).check(matches(withText("test123")))

        // Perform click on the login button
        onView(withId(R.id.buttonLogin)).perform(click())

        // Verify that MainActivity is started
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))

        // Optionally, verify no more interactions with intents
        Intents.assertNoUnverifiedIntents()
    }
}
