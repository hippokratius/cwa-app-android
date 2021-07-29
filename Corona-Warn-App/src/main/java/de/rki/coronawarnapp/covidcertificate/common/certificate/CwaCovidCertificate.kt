package de.rki.coronawarnapp.covidcertificate.common.certificate

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import org.joda.time.Instant

/**
 * For use with the UI
 */
interface CwaCovidCertificate {
    // Header
    val headerIssuer: String
    val headerIssuedAt: Instant
    val headerExpiresAt: Instant

    val qrCodeToDisplay: CoilQrCode
    val firstName: String?
    val lastName: String
    val fullName: String
    val fullNameFormatted: String
    val dateOfBirthFormatted: String
    val personIdentifier: CertificatePersonIdentifier
    val certificateIssuer: String
    val certificateCountry: String
    val certificateId: String

    /**
     * The ID of the container holding this certificate in the CWA.
     */
    val containerId: CertificateContainerId

    val rawCertificate: DccV1.MetaData

    val dccData: DccData<out DccV1.MetaData>

    val notifiedExpiresSoonAt: Instant?

    val notifiedExpiredAt: Instant?

    /**
     * The current state of the certificate, see [State]
     */
    fun getState(): State

    val isValid get() = getState() is State.Valid || getState() is State.ExpiringSoon

    sealed class State {

        data class Valid(
            val expiresAt: Instant,
        ) : State()

        data class ExpiringSoon(
            val expiresAt: Instant,
        ) : State()

        data class Expired(
            val expiredAt: Instant
        ) : State()

        object Invalid : State() {
            const val URL_INVALID_SIGNATURE_DE = "https://www.coronawarn.app/de/faq/#hc_signature_invalid"
            const val URL_INVALID_SIGNATURE_EN = "https://www.coronawarn.app/en/faq/#hc_signature_invalid"
        }
    }
}
