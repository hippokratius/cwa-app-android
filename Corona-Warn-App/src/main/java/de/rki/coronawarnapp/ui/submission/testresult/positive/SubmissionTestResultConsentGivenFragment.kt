package de.rki.coronawarnapp.ui.submission.testresult.positive

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionTestResultConsentGivenBinding
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionTestResultConsentGivenFragment], the test result screen that is shown to the user if they have provided
 * consent.
 */
class SubmissionTestResultConsentGivenFragment :
    Fragment(R.layout.fragment_submission_test_result_consent_given),
    AutoInject {

    private val navArgs by navArgs<SubmissionTestResultConsentGivenFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionTestResultConsentGivenViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionTestResultConsentGivenViewModel.Factory
            factory.create(navArgs.testType)
        }
    )

    private val binding: FragmentSubmissionTestResultConsentGivenBinding by viewBinding()

    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        viewModel.onTestOpened()

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onShowCancelDialog()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.uiState.observe2(this) {
            binding.apply {
                uiState = it
                submissionTestResultSection.setTestResultSection(it.coronaTest)
            }
        }

        setButtonOnClickListener()

        viewModel.showCancelDialog.observe2(this) { showCancelDialog() }

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSymptomIntroduction ->
                    doNavigate(
                        SubmissionTestResultConsentGivenFragmentDirections
                            .actionSubmissionTestResultConsentGivenFragmentToSubmissionSymptomIntroductionFragment(
                                navArgs.testType
                            )
                    )
                is SubmissionNavigationEvents.NavigateToMainActivity ->
                    doNavigate(
                        SubmissionTestResultConsentGivenFragmentDirections
                            .actionSubmissionTestResultConsentGivenFragmentToHomeFragment()
                    )
                else -> Unit
            }
        }

        viewModel.showUploadDialog.observe2(this) {
            uploadDialog.setState(it)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionTestResultContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
        viewModel.onNewUserActivity()
    }

    private fun setButtonOnClickListener() {
        binding.submissionTestResultButtonConsentGivenContinue.setOnClickListener {
            viewModel.onContinuePressed()
        }

        binding.submissionTestResultButtonConsentGivenContinueWithoutSymptoms.setOnClickListener {
            viewModel.onShowCancelDialog()
        }

        binding.submissionTestResultConsentGivenHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onShowCancelDialog()
        }
    }

    private fun showCancelDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.submission_error_dialog_confirm_cancellation_title)
            setMessage(R.string.submission_error_dialog_confirm_cancellation_body)
            setPositiveButton(R.string.submission_error_dialog_confirm_cancellation_button_positive) { _, _ ->
                viewModel.onCancelConfirmed()
            }
            setNegativeButton(R.string.submission_error_dialog_confirm_cancellation_button_negative) { _, _ ->
                // NOOP
            }
        }.show()
    }
}
