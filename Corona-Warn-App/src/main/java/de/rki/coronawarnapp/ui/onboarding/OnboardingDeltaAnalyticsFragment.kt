package de.rki.coronawarnapp.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingDeltaPpaBinding
import de.rki.coronawarnapp.datadonation.analytics.common.labelStringRes
import de.rki.coronawarnapp.datadonation.analytics.ui.input.AnalyticsUserInputFragment
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class OnboardingDeltaAnalyticsFragment : Fragment(R.layout.fragment_onboarding_delta_ppa), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: OnboardingAnalyticsViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingDeltaPpaBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onProceed(false)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.apply {
            onboardingButtonNext.setOnClickListener { viewModel.onProceed(true) }
            onboardingButtonDisable.setOnClickListener { viewModel.onProceed(false) }
            onboardingButtonBack.buttonIcon.setOnClickListener { requireActivity().onBackPressed() }

            federalStateRow.setOnClickListener {
                doNavigate(
                    OnboardingDeltaAnalyticsFragmentDirections
                        .actionOnboardingDeltaAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.FEDERAL_STATE
                        )
                )
            }
            districtRow.setOnClickListener {
                doNavigate(
                    OnboardingDeltaAnalyticsFragmentDirections
                        .actionOnboardingDeltaAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.DISTRICT
                        )
                )
            }
            ageGroupRow.setOnClickListener {
                doNavigate(
                    OnboardingDeltaAnalyticsFragmentDirections
                        .actionOnboardingDeltaAnalyticsFragmentToAnalyticsUserInputFragment(
                            type = AnalyticsUserInputFragment.InputType.AGE_GROUP
                        )
                )
            }
            moreInfoRow.setOnClickListener {
                doNavigate(
                    OnboardingDeltaAnalyticsFragmentDirections
                        .actionOnboardingDeltaAnalyticsFragmentToPpaMoreInfoFragment()
                )
            }
        }
        viewModel.completedOnboardingEvent.observe2(this) {
            (requireActivity() as OnboardingActivity).completeOnboarding()
        }
        viewModel.ageGroup.observe2(this) {
            binding.ageGroupRowBody.text = getString(it.labelStringRes)
        }
        viewModel.federalState.observe2(this) {
            binding.districtRow.visibility = if (it != PpaData.PPAFederalState.FEDERAL_STATE_UNSPECIFIED) {
                View.VISIBLE
            } else {
                View.GONE
            }
            binding.federalStateRowBody.text = getString(it.labelStringRes)
        }
        viewModel.district.observe2(this) {
            binding.districtRowBody.text = it?.districtName
                ?: getString(R.string.analytics_userinput_district_unspecified)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingPpaContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
