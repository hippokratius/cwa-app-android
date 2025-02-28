package de.rki.coronawarnapp.test.booster.ui

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelKey

@Module
abstract class BoosterTestModule {
    @Binds
    @IntoMap
    @CWAViewModelKey(BoosterTestViewModel::class)
    abstract fun dscTest(factory: BoosterTestViewModel.Factory): CWAViewModelFactory<out CWAViewModel>
}
