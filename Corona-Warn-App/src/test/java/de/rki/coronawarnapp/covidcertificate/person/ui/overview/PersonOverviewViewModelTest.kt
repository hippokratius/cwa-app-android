package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import de.rki.coronawarnapp.covidcertificate.common.repository.TestCertificateContainerId
import de.rki.coronawarnapp.covidcertificate.expiration.DccExpirationNotificationService
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates.AdmissionState.Other
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificatesProvider
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CovidTestCertificatePendingCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.PersonCertificateCard
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.valueset.ValueSetsRepository
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.spyk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.extensions.InstantExecutorExtension
import testhelpers.extensions.getOrAwaitValue

@ExtendWith(InstantExecutorExtension::class)
class PersonOverviewViewModelTest : BaseTest() {
    @MockK lateinit var personCertificatesProvider: PersonCertificatesProvider
    @MockK lateinit var testCertificateRepository: TestCertificateRepository
    @MockK lateinit var refreshResult: TestCertificateRepository.RefreshResult
    @MockK lateinit var valueSetsRepository: ValueSetsRepository
    @MockK lateinit var expirationNotificationService: DccExpirationNotificationService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this, true)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")

        coEvery { testCertificateRepository.refresh(any()) } returns setOf(refreshResult)
        every { personCertificatesProvider.personCertificates } returns emptyFlow()
        every { refreshResult.error } returns null
        every { testCertificateRepository.certificates } returns flowOf(setOf())
        every { valueSetsRepository.triggerUpdateValueSet(any()) } just Runs
        coEvery { expirationNotificationService.showNotificationIfStateChanged(any()) } just runs
    }

    // TODO: Update tests
    @Test
    fun `refreshCertificate causes an error dialog event`() {
        val error = mockk<Exception>()
        every { refreshResult.error } returns error

        instance.apply {
            refreshCertificate(TestCertificateContainerId("Identifier"))
            events.getOrAwaitValue() shouldBe ShowRefreshErrorDialog(error)
        }
    }

    @Test
    fun `refreshCertificate triggers refresh operation in repo`() {
        instance.refreshCertificate(TestCertificateContainerId("Identifier"))
        coVerify { testCertificateRepository.refresh(any()) }
    }

    @Test
    fun `deleteTestCertificate deletes certificates from repo`() {
        coEvery { testCertificateRepository.deleteCertificate(any()) } returns mockk()
        instance.apply {
            deleteTestCertificate(TestCertificateContainerId("Identifier"))
        }

        coVerify { testCertificateRepository.deleteCertificate(any()) }
    }

    @Test
    fun `Sorting - List has pending certificate`() {
        every { testCertificateRepository.certificates } returns flowOf(
            setOf(PersonCertificatesData.mockTestCertificateWrapper)
        )
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithPending
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                        every { admissionState } returns Other(certificates.first())
                    }
                }.run { flowOf(this.toSet()) }

        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as CovidTestCertificatePendingCard.Item).apply {
                certificate.containerId shouldBe TestCertificateContainerId(
                    "testCertificateContainerId"
                )
            }
            (get(1) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Zeebee"
            }
            (get(2) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Andrea Schneider"
            }
        }
    }

    @Test
    fun `Sorting - List has pending & updating certificate`() {
        every { testCertificateRepository.certificates } returns flowOf(
            setOf(PersonCertificatesData.mockTestCertificateWrapper)
        )
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithUpdating
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                        every { admissionState } returns Other(certificates.first())
                    }
                }.run { flowOf(this.toSet()) }

        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as CovidTestCertificatePendingCard.Item).apply {
                certificate.containerId shouldBe TestCertificateContainerId(
                    "testCertificateContainerId"
                )
            }
            (get(1) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Zeebee"
            }
            (get(2) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Andrea Schneider"
            }
        }
    }

    @Test
    fun `Sorting - List has no CWA user`() {
        every { testCertificateRepository.certificates } returns flowOf(setOf())
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithoutCwaUser
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                        every { admissionState } returns Other(certificates.first())
                    }
                }.run { flowOf(this.toSet()) }

        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Andrea Schneider"
            }
            (get(1) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Erika Musterfrau"
            }
            (get(2) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Max Mustermann"
            }
        }
    }

    @Test
    fun `Sorting - List has CWA user`() {
        every { personCertificatesProvider.personCertificates } returns
            PersonCertificatesData.certificatesWithCwaUser
                .map {
                    spyk(it).apply {
                        every { highestPriorityCertificate } returns certificates.first()
                        every { admissionState } returns Other(certificates.first())
                    }
                }.run { flowOf(this.toSet()) }

        instance.personCertificates.getOrAwaitValue().apply {
            (get(0) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Zeebee"
            } // CWA user
            (get(1) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Andrea Schneider"
            }
            (get(2) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Erika Musterfrau"
            }
            (get(3) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Max Mustermann"
            }
            (get(4) as PersonCertificateCard.Item).apply {
                admissionState.primaryCertificate.fullName shouldBe "Zeebee A"
            }
        }
    }

    @Test
    fun `checkExpiration calls expiration notification service`() {
        instance.run {
            checkExpiration()

            coVerify {
                expirationNotificationService.showNotificationIfStateChanged(ignoreLastCheck = true)
            }
        }
    }

    private val instance
        get() = PersonOverviewViewModel(
            dispatcherProvider = TestDispatcherProvider(),
            testCertificateRepository = testCertificateRepository,
            certificatesProvider = personCertificatesProvider,
            appScope = TestCoroutineScope(),
            expirationNotificationService = expirationNotificationService
        )
}
