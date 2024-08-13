package nl.rabobank.investments.cactus.commons.bbanibanconverter

import nl.garvelink.iban.IBAN
import nl.garvelink.iban.Modulo97

object BbanToIban {
    fun convertBBANToIBAN(bban: String, swiftCode: String): String {
        val paddedBban = bban.padStart(
            BBAN_LENGTH_NETHERLANDS,
            PADDING_CHAR
        )
        val swiftCodePlusBban = "$swiftCode$paddedBban"
        return makeIban(
            COUNTRY_CODE, swiftCodePlusBban
        ).toPlainString()
    }
    private fun makeIban(countryCode: String, swiftCodePlusBban: String): IBAN {
        val sb = StringBuilder().append(countryCode).append("00").append(swiftCodePlusBban)
        val checkDigits = Modulo97.calculateCheckDigits(sb)
        val checkDigitsString = "$checkDigits".padStart(IBAN_CHECK_DIGITS_LENGTH, PADDING_CHAR)
        sb.replace(IBAN_CHECK_DIGIT_START_INDEX, IBAN_CHECK_DIGIT_END_INDEX, checkDigitsString)
        return IBAN.parse(sb)
    }

    private const val BBAN_LENGTH_NETHERLANDS = 10
    private const val COUNTRY_CODE = "NL"
    private const val RABO_SWIFT_CODE = "RABO"
    private const val IBAN_CHECK_DIGITS_LENGTH = 2
    private const val IBAN_CHECK_DIGIT_START_INDEX = 2
    private const val IBAN_CHECK_DIGIT_END_INDEX = 4
    private const val PADDING_CHAR = '0'
}