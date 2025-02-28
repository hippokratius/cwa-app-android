package de.rki.coronawarnapp.ui.submission.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.antigen.profile.RATProfileSettingsDataStore
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class SubmissionDispatcherViewModel @AssistedInject constructor(
    private val ratProfileSettings: RATProfileSettingsDataStore,
    dispatcherProvider: DispatcherProvider,
) : CWAViewModel(dispatcherProvider) {

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()
    val profileCardId: LiveData<Int> = ratProfileSettings.profileFlow
        .map { profile ->
            Timber.d("profile=$profile")
            if (profile == null)
                R.layout.submission_create_rat_profile_card
            else
                R.layout.submission_open_rat_profile_card
        }.asLiveData()

    fun onBackPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToMainActivity)
    }

    fun onTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToTAN)
    }

    fun onTeleTanPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToContact)
    }

    fun onQRCodePressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToQRCodeScan)
    }

    fun onClickProfileCard() = launch {
        val event = if (ratProfileSettings.profileFlow.first() != null) {
            SubmissionNavigationEvents.NavigateToOpenProfile
        } else {
            SubmissionNavigationEvents.NavigateToCreateProfile(ratProfileSettings.onboardedFlow.first())
        }
        routeToScreen.postValue(event)
    }

    fun onTestCenterPressed() {
        routeToScreen.postValue(SubmissionNavigationEvents.OpenTestCenterUrl)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionDispatcherViewModel>
}
