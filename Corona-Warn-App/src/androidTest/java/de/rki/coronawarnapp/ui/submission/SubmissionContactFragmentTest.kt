package de.rki.coronawarnapp.ui.submission

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.ui.submission.fragment.SubmissionContactFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionContactViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest

@RunWith(AndroidJUnit4::class)
class SubmissionContactFragmentTest : BaseUITest() {

    @MockK lateinit var viewModel: SubmissionContactViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        setupMockViewModel(object : SubmissionContactViewModel.Factory {
            override fun create(): SubmissionContactViewModel = viewModel
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
//        launchFragmentInContainer2<SubmissionContactFragment>()
    }

    @Test fun testContactCallClicked() {
//        val scenario = launchFragmentInContainer2<SubmissionContactFragment>()
//        onView(withId(R.id.submission_contact_button_call))
//            .perform(click())
//
//        // TODO verify result
    }

    @Test fun testContactEnterTanClicked() {
//        val scenario = launchFragmentInContainer2<SubmissionContactFragment>()
//        onView(withId(R.id.submission_contact_button_enter))
//            .perform(click())
//
//        // TODO verify result
    }
}

@Module
abstract class SubmissionContactTestModule {
    @ContributesAndroidInjector
    abstract fun submissionContactScreen(): SubmissionContactFragment
}
