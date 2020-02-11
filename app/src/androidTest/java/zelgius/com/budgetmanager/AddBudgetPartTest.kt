package zelgius.com.budgetmanager


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddBudgetPartTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun addBudgetPartTest() {
        val actionMenuItemView = onView(
                allOf(withId(R.id.menu_donut), withContentDescription("Edit part repartition"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomBar),
                                        0),
                                0),
                        isDisplayed()))
        actionMenuItemView.perform(click())

        val appCompatImageButton = onView(
                allOf(withContentDescription("Navigate up"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withContentDescription("app"),
                                                0)),
                                2),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val actionMenuItemView2 = onView(
                allOf(withId(R.id.menu_edit), withContentDescription("Edit Budget"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomBar),
                                        0),
                                1),
                        isDisplayed()))
        actionMenuItemView2.perform(click())

        val floatingActionButton = onView(
                allOf(withId(R.id.add),
                        childAtPosition(
                                allOf(withId(R.id.coordinator),
                                        childAtPosition(
                                                withId(R.id.nav_host_fragment),
                                                0)),
                                2),
                        isDisplayed()))
        floatingActionButton.perform(click())

        val textInputEditText = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.name),
                                0),
                        0),
                        isDisplayed()))
        textInputEditText.perform(replaceText("Espresso"), closeSoftKeyboard())

        val materialButton = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        materialButton.perform(scrollTo(), click())

        val actionMenuItemView3 = onView(
                allOf(withId(R.id.menu_donut), withContentDescription("Edit part repartition"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.bottomBar),
                                        0),
                                0),
                        isDisplayed()))
        actionMenuItemView3.perform(click())

        val floatingActionButton2 = onView(
                allOf(withId(R.id.fab),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.coordinator),
                                        1),
                                2),
                        isDisplayed()))
        floatingActionButton2.perform(click())

        val textInputEditText2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.name),
                                0),
                        0),
                        isDisplayed()))
        textInputEditText2.perform(replaceText("rrr"), closeSoftKeyboard())

        val textInputEditText3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.goal),
                                0),
                        0),
                        isDisplayed()))
        textInputEditText3.perform(replaceText("28"), closeSoftKeyboard())

        val materialButton2 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)))
        materialButton2.perform(scrollTo(), click())
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
